package cubepiler.syntaxchecker;

import java.util.LinkedList;

import cubepiler.lexer.Lexer;
import cubepiler.lexer.SourceException;
import cubepiler.lexer.Token;
import cubepiler.lexer.Token.TokenType;

public class Parser {
	private Lexer lexer;
	private LinkedList<Token> tokenList;
	private Token tokenRead;
	private int index = 0;
	private boolean end = false, validity = true;
	public Parser() throws SourceException{
		lexer = new Lexer();
		String test = "while true & false \n\n\n end";
		
		printTokens(test+" ");
		tokenRead = tokenList.getFirst();
		System.out.println("tokenRead: "+tokenRead.getValue());
	}
	public void printTokens(String string) throws SourceException{
		tokenList = new LinkedList<Token>();
		
		tokenList = lexer.getTokens(string);
		for(int i =0; i < tokenList.size(); i++){
			System.out.println("L "+tokenList.get(i).getType());
		}
	}
	
	 private void move() {
		 index += 1;
		 if(index < tokenList.size()){
			 end = false;
		    tokenRead = tokenList.get(index); 
		    System.out.println("tokenRead: "+tokenRead.getValue());
		   
		 }else {
			 System.out.println("No more tokens!");
			 end = true;
		 } 
		
		 	
	 }
	 
	 
	private void match(TokenType type, String line) {
        if (tokenRead.getType() == type) {
            move();
            
            validity =  true;
        } else {
        	syntaxError("line "+line+" :token type mismatch");
           validity =  false;
        }
    }
	
	public boolean checkDataType(TokenType type){
		if(type == TokenType.INTEGER || type == TokenType.FLOAT || type == TokenType.STRING || type == TokenType.TRUE || type == TokenType.FALSE || type == TokenType.USER_DEFINED_NAME){
			return true;
		}
		else
		{
			 return false;
		}
	}
	public void start(){
		program();
	}
	public void program(){	// <program> := <declaration> | <declaration> <function> | <function>
		programBlock();
	}
	public void programBlock(){
		while(tokenRead.getType() == TokenType.NEW_LINE) {
			move();
		}
		//declarations();
		//functions();
		whileLoop();
	}
	
	private void whileLoop() {
		match(TokenType.WHILE, "83");
		conditions();
		match(TokenType.END, "86");
		if(end) valid();
	}
	
	private void conditions() {
		relationalExpressions();
		while(tokenRead.getType() != TokenType.NEW_LINE) {
			booleanOperators();
			relationalExpressions();
		}
		while(tokenRead.getType() == TokenType.NEW_LINE) {
			move();
			if(end) syntaxError(" missing end statement");
		}
	}
	
	private void relationalExpressions() {
		if(checkBooleanType(tokenRead.getType())) {
			move();
			if(end) syntaxError("incomplete expression 106");
			
			if(tokenRead.getType() == TokenType.NEW_LINE) {
				return;
			}
			
		}else if(checkDataType(tokenRead.getType())) {
			move();
			if(end) syntaxError("incomplete expression 113");
			
			relationalOperator();
			if(checkDataType(tokenRead.getType())) {
				move();
				if(end) syntaxError("incomplete expression 118");
				
				return;
			}else {
				syntaxError("incomplete expression 118");
			}
		}
	}
	
	private void booleanOperators() {
		if(checkBooleanOperators(tokenRead.getType())) {
			move();
			if(end) syntaxError("incomplete expression 120");
			
		}else {
			syntaxError("not a boolean operator 123");
		}
	}
	private boolean checkBooleanOperators(TokenType type) {
		
		if(type == TokenType.AND || type == TokenType.OR) {
			return true;
		}
		return false;
	}
	
	private void relationalOperator() {
		if(checkRelOperator(tokenRead.getType())) {
			move();
			if(end) syntaxError("incomplete expression");
			
		}else {
			syntaxError("while lacking condition");
		}
	}
	
	private boolean checkRelOperator(TokenType type) {
		if(type == TokenType.N_EQUAL || type == TokenType.EQUAL || type == TokenType.G_EQUAL ||
				type == TokenType.G_THAN || type == TokenType.L_EQUAL || type == TokenType.L_THAN) {
			return true;
		}
		return false;
	}
	
	private boolean checkBooleanType(TokenType type) {
		if(type == TokenType.FALSE || type == TokenType.TRUE) {
			return true;
		}
		return false;
	}

	private void declarations() {
		if(tokenRead.getType() == TokenType.FN) {
			return;
		}
		match(TokenType.VAR, "81");
		match(TokenType.USER_DEFINED_NAME, "82");
		
		if(end) valid();
		
		if(tokenRead.getType() == TokenType.NEW_LINE) {
			while(tokenRead.getType() == TokenType.NEW_LINE) {
				move();
				if(end) valid();
			}
			declarations();
		}else if(tokenRead.getType() == TokenType.SEPARATOR) {
			while(tokenRead.getType() != TokenType.NEW_LINE) {
				match(TokenType.SEPARATOR, "90");
				if(end) syntaxError("excess ,");
				match(TokenType.USER_DEFINED_NAME, "91");
				if(end) valid();
				
			}
			
			move();
			if(end) valid();
			while(tokenRead.getType() == TokenType.NEW_LINE) {
				move();
				if(end) valid();
			}
			
			declarations();
		}else if(tokenRead.getType() == TokenType.USER_DEFINED_NAME) {
			
			syntaxError("missing ,");
		}else if(tokenRead.getType() == TokenType.ASSIGNMENT) {
			match(TokenType.ASSIGNMENT, "109");
			if(end) syntaxError(" undefined = ");
			if(checkDataType(tokenRead.getType())) {
				move();
				if(end) valid();
				
			}else {
				syntaxError(" missing value");
			}
			
			while(tokenRead.getType() == TokenType.NEW_LINE) {
				match(TokenType.NEW_LINE, "120");
				if(end) valid();
			}
			
			declarations();
		}
	}
	
	private void valid() {
		System.out.println("valid");
		System.exit(0);
	}
	private void syntaxError(String error) {
		System.out.println("SYNTAX ERROR. "+error);
		System.exit(0);
	}
	
	public void functions(){ // <function> := fn <id> do <block> <end>
		System.out.println("in func");
		System.exit(0);
	}
	
	public static void main(String[] args) throws SourceException{
		new Parser().start();
	}
}