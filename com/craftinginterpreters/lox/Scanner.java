package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> keywords;

    static
    {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        
        keywords.put("class", TokenType.CLASS);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);

        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
       
        keywords.put("fun", TokenType.FUN);
        
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("nil", TokenType.NIL);
        
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("break", TokenType.BREAK);

        keywords.put("var", TokenType.VAR);
        keywords.put("return", TokenType.RETURN);

        keywords.put("print", TokenType.PRINT);
    }

    private int start;
    private int current;
    private int line;

    Scanner(String source)
    {
        this.source = source;
    }

    List<Token> scanTokens()
    {
        while(!isAtEnd())
        {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken()
    {
        char c = advance();
        switch(c)
        {
            // Single Character match
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ':': addToken(TokenType.COLON); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            
            // One-look-ahead match
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=')? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=')? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;

            // Match comment or slash
            case '/':
                if(match('/'))
                {
                    while(peek() != '\n' && !isAtEnd()) 
                    {
                        advance();
                    }
                }
                else
                {
                    addToken(TokenType.SLASH);
                }
                break;
            
            // Eat spaces
            case ' ':
            case '\r':
            case '\t':
                break;
        
            // Process line
            case '\n':
                line++;
                break;
            
            // Process string 
            case '"':
                string();
                break;

            // Unexpected stream
            default:
                if(isDigit(c))
                {
                    number();
                }
                else if(isAlpha(c))
                {
                    identifier();
                }
                else
                {
                    Lox.error(line, "Unexpected Character");
                }
        }
    }

    private boolean isAtEnd()
    {
        return current >= source.length();
    }

    private char advance()
    {
        return source.charAt(current++);
    }

    private void addToken(TokenType type)
    {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal)
    {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected)
    {
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;
        
        // Move ahead
        current++;
        return true;
    }

    private char peek()
    {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void string()
    {
        while(peek() != '"' && !isAtEnd())
        {
            if(peek() == '\n')
            {
                line++;
            }
            advance();
        }

        if(isAtEnd())
        {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // cover closing ".
        advance();

        // Extract the string
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean isDigit(char c)
    {
        return c >= '0' && c <= '9';
    }

    private void number()
    {
        while(isDigit(peek())) advance();

        // process fractional part
        if(peek() == '.' && isDigit(peekNext())) 
        {
            // eat .
            advance();

            // take digits in the fractional part.
            while(isDigit(peek()))
            {
                advance();
            }
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext()
    {
        if(current + 1 >= source.length())
        {
            return '\0';
        }

        // Check one character ahead.
        return source.charAt(current +1);
    }

    private boolean isAlpha(char c)
    {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c)
    {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier()
    {
        while(isAlphaNumeric(peek()))
        {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        if(type == null)
        {
            type = TokenType.IDENTIFIER;
        }
        addToken(type);
    }
}
