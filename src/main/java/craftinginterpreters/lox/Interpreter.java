package craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;


public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{
    static class LoxInterpreterControlException extends RuntimeException{} //хак, позволяющий не определять throws
    static class BreakLoopException extends LoxInterpreterControlException{};
    static class ContinueLoopException extends LoxInterpreterControlException{};
    static class Return extends RuntimeException{
        final Object value;
        Return(Object value){
            super(null, null, false, false);
            this.value = value;
        }
    }


    final Environment globals = new Environment();
    private Environment environment = globals;

    Interpreter(){
        LoxPredefined.bake(this);
    }



    void interpret(List<Stmt> statements){
        try{
            for(Stmt statement: statements){
                execute(statement);
            }
        }catch (RuntimeError error){
            Lox.runtimeError(error);
        }
    }

    String interpret(Expr expr){
        return stringify(expr.accept(this));
    }



    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type){
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;

            case PLUS:
                if(left instanceof Double && right instanceof Double){
                    return (double)left + (double)right;
                }
                if(left instanceof String && right instanceof String){
                    return (String)left + (String)right;
                }

                if(left instanceof String && right instanceof Double
                    || left instanceof Double && right instanceof String){
                    return stringify(left)+stringify(right);
                }

                throw new RuntimeError(expr.operator,
                    "Operands must be two numbers or two strings or string and number");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                checkZeroDivision(expr.operator, (Double)right);
                return (double)left / (double)right;
            case STAR:
                if(left instanceof Double){
                    checkNumberOperands(expr.operator, left, right);
                    return (double)left * (double)right;
                }
                if(left instanceof String){
                    checkNumberOperand(expr.operator, right);
                    int repeat_number = (int) Math.round((Double)right);
                    if(repeat_number<=0) throw new RuntimeError(expr.operator,
                        "Right operand of string repetition should be >=1");
                    return ((String)left).repeat(repeat_number);
                }
                throw new RuntimeError(expr.operator,
                    "Operands must be two numbers or string and a number");

            case GREATER:
                if(left instanceof Double && right instanceof Double){
                    return (double)left > (double)right;
                }
                if(left instanceof String && right instanceof String){
                    return ((String) left).compareTo((String)right)>0;
                }
                throw new RuntimeError(expr.operator,
                    "Operands must be two numbers or two strings");

            case GREATER_EQUAL:
                if(left instanceof Double && right instanceof Double){
                    return (double)left >= (double)right;
                }
                if(left instanceof String && right instanceof String){
                    return ((String) left).compareTo((String)right)>=0;
                }
                throw new RuntimeError(expr.operator,
                    "Operands must be two numbers or two strings");

            case LESS:
                if(left instanceof Double && right instanceof Double){
                    return (double)left < (double)right;
                }
                if(left instanceof String && right instanceof String){
                    return ((String) left).compareTo((String)right)<0;
                }
                throw new RuntimeError(expr.operator,
                    "Operands must be two numbers or two strings");

            case LESS_EQUAL:
                if(left instanceof Double && right instanceof Double){
                    return (double)left <= (double)right;
                }
                if(left instanceof String && right instanceof String){
                    return ((String) left).compareTo((String)right)<=0;
                }
                throw new RuntimeError(expr.operator,
                    "Operands must be two numbers or two strings");

            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);

            case COMMA:
                return right;

        }


        return null;
    }



    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {

        if(expr.op1.type==TokenType.QUESTION && expr.op2.type==TokenType.COLON){
            Object condition = evaluate(expr.left);
            if(isTruthy(condition)){
                return evaluate(expr.middle);
            }else{
                return evaluate(expr.right);
            }
        }

        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch(expr.operator.type){ //пока только минус, позже можно будет добавить ещё
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right; //-double
            case BANG: return !isTruthy(right); //!boolean
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr){
        Object variable = environment.get(expr.name);
        if(variable instanceof LoxMacro){
            return ((LoxMacro)variable).call(this);
        }else{
            return variable;
        }

    }


    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.calee);

        List<Object> arguments = new ArrayList<>();
        for(Expr argument : expr.arguments){
            arguments.add(evaluate(argument));
        }
        if(!(callee instanceof LoxCallable)){
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }
        LoxCallable function = (LoxCallable)callee;
        if(arguments.size()!=function.arity()){
            throw new RuntimeError(expr.paren,
                String.format("Expected %d arguments but got %d.", function.arity(), arguments.size()));
        }
        return function.call(this, arguments);
    }

    @Override
    public Object visitAnonFunExpr(Expr.AnonFun expr) {
        //LoxFunction function = new LoxFunction(stmt, environment);
        return new LoxFunction(new Stmt.Function(null, expr.params, expr.body), environment);
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    private void execute(Stmt stmt){
        stmt.accept(this);
    }


    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment){
        Environment previous = this.environment;
        try{
            this.environment = environment;

            for(Stmt statement : statements){
                execute(statement);
            }
        } finally{
            this.environment = previous;
        }
    }

    private boolean isTruthy(Object object){
        if(object==null) return false;
        if(object instanceof Boolean) return (boolean)object;
        if(object instanceof Double){
            double  value = (double)object;
            return !Double.isNaN(value) && value!=0.0;
        }
        if(object instanceof String){
            return !"".equals(object);
        }
        return true; // для ввсех остальных объектов положим true
    }
    private boolean isEqual(Object a, Object b){
        if(a==null && b==null) return true; // nil == nil
        if(a==null) return false; // nil != Object
        return a.equals(b);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt){
        if(isTruthy(evaluate(stmt.condition))){
            execute(stmt.thenBranch);
        }else if(stmt.elseBranch!=null){
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr){
        Object left = evaluate(expr.left);
        if(expr.operator.type==TokenType.OR){
            if(isTruthy(left)) return left;
        } else if(expr.operator.type==TokenType.AND){
            if(!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        while(isTruthy(evaluate(stmt.condition))){
            try{
                execute(stmt.body);
            }catch (BreakLoopException e){
                break;
            }catch (ContinueLoopException ignored){}

        }
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        if(stmt.init!=null)
            execute(stmt.init);

        while(isTruthy(evaluate(stmt.condition))){
            try{
                execute(stmt.body);
            }catch (BreakLoopException e){
                break;
            }catch (ContinueLoopException ignored){}
            if(stmt.increment!=null) //исполняем инкремент даже если мы использовали continue
                evaluate(stmt.increment);
        }
        return null;
    }

    @Override
    public Void visitControlStatementStmt(Stmt.ControlStatement stmt)  {
        switch (stmt.parameter.type){
            case BREAK -> throw new BreakLoopException();
            case CONTINUE -> throw new ContinueLoopException();
            default -> {return null;}
        }
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        Object value = null;
        if(stmt.value!=null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        Object value = null;
        if(stmt.initializer != null){
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitMacroStmt(Stmt.Macro stmt) {
        LoxMacro macro = new LoxMacro() {
            Environment env = environment;
            @Override
            public Object call(Interpreter interpreter) {
                try{
                    executeBlock(stmt.body, env);
                }catch (Return r){
                    return r.value;
                }
                return null;
            }
        };
        environment.define(stmt.name.lexeme, macro);
        return null;

    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr){
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }


    private String stringify(Object object){
        if(object==null) return "nil";
        if(object instanceof Double){
            String text = object.toString();
            if(text.endsWith(".0")){
                text = text.substring(0, text.length()-2);
            }
            return text;
        }
        return object.toString();
    }


    private void checkNumberOperand(Token operator, Object operand){
        if(operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }
    private void checkNumberOperands(Token operator, Object left, Object right){
        if(left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers");
    }

    private void checkZeroDivision(Token operator, Double right) {
        if (right != 0.0) return;
        throw new RuntimeError(operator, "Zero division");
    }
}
