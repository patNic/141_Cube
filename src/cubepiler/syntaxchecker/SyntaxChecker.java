package cubepiler.syntaxchecker;

import java.util.LinkedList;

import cubepiler.lexer.SourceException;
import cubepiler.lexer.Token;

public class SyntaxChecker {
	private LinkedList<Token> tokens;
	private int currentTokenIndex;
	private Token currentToken;
	private boolean isCodeBlock = false;
  private int fwfCount, endCount;
	private boolean isMultipleAssignment = false;
	
	public SyntaxChecker(LinkedList<Token> tokens) {
		this.tokens = tokens;
		this.tokens.add(new Token("EOF", Token.TokenType.EOF, 0, 0));
		this.currentTokenIndex = 0;
		this.fwfCount = 0;
		this.endCount = 0;
	}
	
	public void scan() throws SourceException {
		currentToken = tokens.get(currentTokenIndex++);
		
		if (currentToken.getType() == Token.TokenType.EOF) {
			if(fwfCount != endCount) {
				System.out.println("F "+ fwfCount + " E "+ endCount);
				throw new SourceException("Inconsistent end", currentToken.getStartingRow(), currentToken.getStartingColumn());
			}else {
				terminate();
			}
			
		}
		System.out.println(currentToken.getValue() + ":" + currentToken.getStartingRow() + ":" + currentToken.getStartingColumn());
	}
	public void start() throws SourceException {
		program();
		scan();
	}
	
	public void terminate() {
		System.out.println("Notice : Source code is syntactically valid!");
		System.exit(1);
	}
	
	private void program() throws SourceException {
		scan();
		declarations();
	}
	
	private void declarations() throws SourceException {
		if (currentToken.getType() == Token.TokenType.VAR) {
			scan();
			variableDeclarations();
		} else if (currentToken.getType() == Token.TokenType.FN && !isCodeBlock) {
			fwfCount+=1;
			scan();
			functionDeclarations();
		} else if (currentToken.getType() == Token.TokenType.NEW_LINE) {
			scan();
			declarations();
		} else if (currentToken.getType() == Token.TokenType.USER_DEFINED_NAME && isCodeBlock) {
			codeBlock();
		} else if (currentToken.getType() == Token.TokenType.END) {
			endCount+=1;
			isCodeBlock = false;
			scan();
			declarations();
		}
		else if (currentToken.getType() == Token.TokenType.WHILE && !isCodeBlock) {
			fwfCount+=1;
			condition();
			codeBlock();
		}
		else if (currentToken.getType() == Token.TokenType.IF && !isCodeBlock) {
			fwfCount+=1;
			condition();
			codeBlock();
		}
		else if (currentToken.getType() == Token.TokenType.ELSIF && !isCodeBlock) {
			condition();
			codeBlock();
		}
		else if (currentToken.getType() == Token.TokenType.ELSE && !isCodeBlock) {
			codeBlock();
		}
		else {
			throw new SourceException("Illegal line of code!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
	
	private void variableDeclarations() throws SourceException {
		if (currentToken.getType() == Token.TokenType.USER_DEFINED_NAME) {
			scan();
			variableStatements();
		} 
		else {
			throw new SourceException("Illegal variable statement!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
	
	private void variableStatements() throws SourceException {
		if (currentToken.getType() == Token.TokenType.SEPARATOR) {
			scan();
			variableDeclarations();
		} else if (currentToken.getType() == Token.TokenType.NEW_LINE) {
			scan();
			declarations();
		} else if (isOperator(currentToken.getType())) {
			scan();
			
			if (currentToken.getType() == Token.TokenType.USER_DEFINED_NAME)
				variableDeclarations();
			
			variableValues();
		} else {
			System.out.println("I came "+ currentToken.getType());
			throw new SourceException("Illegal variable statement!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
	
	private void variableValues() throws SourceException {
		scan();
		if (currentToken.getType() == Token.TokenType.SEPARATOR) {
			scan();
			variableDeclarations();
		} else if (currentToken.getType() == Token.TokenType.NEW_LINE ) {
			scan();
			declarations();
		}
		else if (currentToken.getType() == Token.TokenType.O_PARENTHESIS ) {
			scan();
			variableValues();
		}
		else if (currentToken.getType() == Token.TokenType.C_PARENTHESIS ) {
			scan();
			variableValues();
		}
		else if (currentToken.getType() == Token.TokenType.INTEGER||currentToken.getType() == Token.TokenType.FALSE
				||currentToken.getType() == Token.TokenType.FLOAT||currentToken.getType() == Token.TokenType.TRUE
				||currentToken.getType() == Token.TokenType.STRING) {
			scan();
			variableValues();
		}
		else if (isOperator(currentToken.getType())) {
			scan();
			scan();
			variableValues();
		} else {
			throw new SourceException("Illegal variable statement!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
	
	private void functionDeclarations() throws SourceException {
		if (currentToken.getType() == Token.TokenType.USER_DEFINED_NAME) {
			scan();
			functionArguments();
		} else {
			throw new SourceException("Illegal function declaration!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
	
	private void functionArguments() throws SourceException {
		if (currentToken.getType() == Token.TokenType.O_PARENTHESIS) {
			scan();
			functionArguments();
		} else if (currentToken.getType() == Token.TokenType.VAR) {
			scan();
			functionArgumentsDeclarations();
		} else if (currentToken.getType() == Token.TokenType.C_PARENTHESIS) {
			scan();
			codeBlock();
		} else {
			throw new SourceException("Illegal arguments found!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
	
	private void functionArgumentsDeclarations() throws SourceException {
		if (currentToken.getType() == Token.TokenType.USER_DEFINED_NAME) {
			scan();
			
			if (currentToken.getType() == Token.TokenType.C_PARENTHESIS)
				functionArguments();
			
			functionArgumentsDeclarations();
		} else if (currentToken.getType() == Token.TokenType.SEPARATOR) {
			scan();
			functionArguments();
		} else if (currentToken.getType() == Token.TokenType.NEW_LINE) {
			codeBlock();
		} else {
			throw new SourceException("Illegal arguments found!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
	
	private void codeBlock() throws SourceException {
		// new line is the current token
		isCodeBlock = true;
		
		while (currentToken.getType() != Token.TokenType.END) {
			scan();
      
			statements();
			
		}
		endCount+= 1;
		scan();
		isCodeBlock = false;
		declarations();
	}
	
  private void statements() throws SourceException{
      if (currentToken.getType() == Token.TokenType.VAR) {
				scan();
				variableDeclarations();
				
				System.out.println("after variable declarations");
			} else if (currentToken.getType() == Token.TokenType.USER_DEFINED_NAME) {
				scan();
				if (currentToken.getType() == Token.TokenType.ASSIGNMENT) {
					isMultipleAssignment = true;
					scan();
					variableOperation(Token.TokenType.ASSIGNMENT, 0);
				} else if (currentToken.getType() == Token.TokenType.O_PARENTHESIS) {
					scan();
					functionCall(Token.TokenType.O_PARENTHESIS);
				}
				 else if (currentToken.getType() == Token.TokenType.C_PARENTHESIS) {
						
					}
				else {
					throw new SourceException("Illegal statement!", currentToken.getStartingRow(), currentToken.getStartingColumn());
				}
			} else if (currentToken.getType() == Token.TokenType.NEW_LINE) {
				
			}
      else if(currentToken.getType() == Token.TokenType.IF) {
			fwfCount+=1;
			condition();
		}else if(currentToken.getType() == Token.TokenType.WHILE) {
			fwfCount+=1;
			condition();
		}
		else if(currentToken.getType() == Token.TokenType.ELSIF) {
			condition();
		}
		else if(currentToken.getType() == Token.TokenType.ELSE) {
			scan();
			if(currentToken.getType() == Token.TokenType.NEW_LINE) {
				statements();
			}
			else {
				throw new SourceException("Unmatched else ", currentToken.getStartingRow(), currentToken.getStartingColumn());
			}
		}
  }

	private void variableOperation(Token.TokenType prev, int parentheses) throws SourceException {
		Token.TokenType curr = currentToken.getType();
		if ((isVariableOrValue(currentToken.getType()) && !isVariableOrValue(prev)) || prev == Token.TokenType.O_PARENTHESIS || (isOperator(prev) && prev != Token.TokenType.ASSIGNMENT)) {
			scan();
			variableOperation(curr, 0);
		} else if (currentToken.getType() == Token.TokenType.ASSIGNMENT && isVariableOrValue(prev) && isMultipleAssignment) {
			scan();
			variableOperation(curr, 0);
		} else if (isOperator(currentToken.getType()) && isVariableOrValue(prev) && currentToken.getType() != Token.TokenType.ASSIGNMENT || prev == Token.TokenType.C_PARENTHESIS) {
			isMultipleAssignment = false;
			scan();
			variableOperation(curr, 0);
		} else if (currentToken.getType() == Token.TokenType.O_PARENTHESIS && isOperator(prev) && currentToken.getType() != Token.TokenType.ASSIGNMENT || prev == Token.TokenType.O_PARENTHESIS) {
			scan();
			variableOperation(curr, ++parentheses);
		} else if (currentToken.getType() == Token.TokenType.C_PARENTHESIS && isVariableOrValue(prev) || prev == Token.TokenType.C_PARENTHESIS) {
			scan();
			variableOperation(curr, --parentheses);
		} else if (currentToken.getType() == Token.TokenType.NEW_LINE) {
			return;
		}
		else if(currentToken.getType() == Token.TokenType.USER_DEFINED_NAME || currentToken.getType() == Token.TokenType.INTEGER||
				currentToken.getType() == Token.TokenType.TRUE||currentToken.getType() == Token.TokenType.STRING||
				currentToken.getType() == Token.TokenType.FLOAT || currentToken.getType() == Token.TokenType.FALSE) {
			return;
		}
		else if (currentToken.getType() == Token.TokenType.WHILE || currentToken.getType() == Token.TokenType.IF ) {
			fwfCount+=1;
		}
		else {
			throw new SourceException("Illegal statement!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
	
	private void functionCall(Token.TokenType prev) throws SourceException {
		Token.TokenType curr = currentToken.getType();
		if (isVariableOrValue(currentToken.getType()) && !isVariableOrValue(prev) && prev != Token.TokenType.C_PARENTHESIS) {
			scan();
			functionCall(curr);
		} else if (currentToken.getType() == Token.TokenType.SEPARATOR && (isVariableOrValue(prev) || prev != Token.TokenType.O_PARENTHESIS )) {
			scan();
			functionCall(curr);
		} else if (currentToken.getType() == Token.TokenType.C_PARENTHESIS && (isVariableOrValue(prev) || prev == Token.TokenType.O_PARENTHESIS)) {
			scan();
			return;
		} else if ((isOperator(currentToken.getType()) && currentToken.getType() != Token.TokenType.ASSIGNMENT) || currentToken.getType() == Token.TokenType.O_PARENTHESIS) {
			scan();
			
			if (currentToken.getType() == Token.TokenType.O_PARENTHESIS)
				expressionArgument(Token.TokenType.O_PARENTHESIS, 1);
			else
				expressionArgument(Token.TokenType.O_PARENTHESIS, 0);
		} else if (currentToken.getType() == Token.TokenType.NEW_LINE) {
			return;
		} else {
			throw new SourceException("Illegal function call!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
	private boolean isVariableOrValue(Token.TokenType type) {
		return (type == Token.TokenType.USER_DEFINED_NAME || type == Token.TokenType.INTEGER || type == Token.TokenType.FLOAT || type == Token.TokenType.BOOLEAN || type == Token.TokenType.STRING);
	}
	private void expressionArgument (Token.TokenType prev, int parentheses) throws SourceException {
		Token.TokenType curr = currentToken.getType();
		if (isVariableOrValue(currentToken.getType()) && !isVariableOrValue(prev) && prev != Token.TokenType.C_PARENTHESIS) {
			scan();
			expressionArgument(curr, parentheses);
		} else if (isOperator(currentToken.getType()) && currentToken.getType() != Token.TokenType.ASSIGNMENT && (isVariableOrValue(prev) || prev == Token.TokenType.C_PARENTHESIS)) {
			scan();
			expressionArgument(curr, parentheses);
		} else if (currentToken.getType() == Token.TokenType.O_PARENTHESIS && (!isVariableOrValue(prev) || (isOperator(prev) && prev != Token.TokenType.ASSIGNMENT)) || prev == Token.TokenType.O_PARENTHESIS) {
			scan();
			expressionArgument(curr, ++parentheses);
		} else if (currentToken.getType() == Token.TokenType.C_PARENTHESIS && (isVariableOrValue(prev) || prev == Token.TokenType.C_PARENTHESIS)) {
			scan();
			expressionArgument(curr, --parentheses);
		} else if (currentToken.getType() == Token.TokenType.SEPARATOR) {
			if (parentheses == 0)
				return;
			else
				throw new SourceException("Illegal parameters!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		} else if (currentToken.getType() == Token.TokenType.NEW_LINE) {
			return;
		} 
		else if(currentToken.getType() == Token.TokenType.USER_DEFINED_NAME || currentToken.getType() == Token.TokenType.INTEGER||
				currentToken.getType() == Token.TokenType.TRUE||currentToken.getType() == Token.TokenType.STRING||
				currentToken.getType() == Token.TokenType.FLOAT || currentToken.getType() == Token.TokenType.FALSE) {
			return;
		}else {
			throw new SourceException("Illegal parameters!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
   private void condition() throws SourceException
	{
		scan();
		if(currentToken.getType() == Token.TokenType.O_PARENTHESIS) {
			conditionalStatements();
		}
		else if(currentToken.getType() == Token.TokenType.NEW_LINE) {
			throw new SourceException("Invalid Condition! ", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
	private void conditionalStatements() throws SourceException{
		scan();
		if(currentToken.getType() == Token.TokenType.C_PARENTHESIS) {
			throw new SourceException("Invalid Condition!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
		else if(currentToken.getType() == Token.TokenType.USER_DEFINED_NAME || currentToken.getType() == Token.TokenType.INTEGER||
				currentToken.getType() == Token.TokenType.TRUE||currentToken.getType() == Token.TokenType.STRING||
				currentToken.getType() == Token.TokenType.FLOAT || currentToken.getType() == Token.TokenType.FALSE) {
			conditions();
		}
		else {
			throw new SourceException("Invalid Condition!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
	private void conditions() throws SourceException{
		while(currentToken.getType() != Token.TokenType.C_PARENTHESIS) {
			scan();
			boolean isRelational = checkRelational();
			System.out.println("I came at conditions "+isRelational);
			
			if(isRelational) {
				scan();
				
				logical();
			}
			else
				logical();
		}
	}
	private void logical() throws SourceException{
		Token.TokenType type = currentToken.getType();
		if(type==  Token.TokenType.AND || type==  Token.TokenType.OR||
				type==  Token.TokenType.NOT ) {
				scan();
				
				if(currentToken.getType() == Token.TokenType.USER_DEFINED_NAME || currentToken.getType() == Token.TokenType.INTEGER
						||currentToken.getType() == Token.TokenType.STRING|| currentToken.getType() == Token.TokenType.TRUE||
						currentToken.getType() == Token.TokenType.FLOAT
						|| currentToken.getType() == Token.TokenType.FALSE){}
				else {
					throw new SourceException("Invalid Condition!", currentToken.getStartingRow(), currentToken.getStartingColumn());
				}
		}
		else if(type ==  Token.TokenType.C_PARENTHESIS){
			return;
		}
		else {
			throw new SourceException("Invalid Condition!", currentToken.getStartingRow(), currentToken.getStartingColumn());
		}
	}
	private boolean checkRelational() throws SourceException{
		Token.TokenType type = currentToken.getType();
		if(type==  Token.TokenType.G_THAN || type==  Token.TokenType.L_THAN||
				type==  Token.TokenType.EQUAL || type==  Token.TokenType.N_EQUAL ||
				type==  Token.TokenType.G_EQUAL || type==  Token.TokenType.L_EQUAL) {
				scan();
				
				if(currentToken.getType() == Token.TokenType.USER_DEFINED_NAME || currentToken.getType() == Token.TokenType.INTEGER||
						currentToken.getType() == Token.TokenType.TRUE||currentToken.getType() == Token.TokenType.STRING||
						currentToken.getType() == Token.TokenType.FLOAT||
						currentToken.getType() == Token.TokenType.FALSE){}
				else {
					throw new SourceException("Invalid Condition!", currentToken.getStartingRow(), currentToken.getStartingColumn());
				}
			return true;
		}
		return false;
	}
  
	private boolean isOperator(Token.TokenType type) {
		return (type == Token.TokenType.ADDITION || type == Token.TokenType.SUBTRACTION || type == Token.TokenType.MULTIPLICATION || type == Token.TokenType.DIVISION || type == Token.TokenType.EXPONENT || type == Token.TokenType.MODULO || type == Token.TokenType.AND || type == Token.TokenType.OR || type == Token.TokenType.ASSIGNMENT);
	}
}
