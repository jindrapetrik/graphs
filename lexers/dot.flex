/*
 *  Copyright (C) 2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.graphs.graphviz.dot.parser;
import java.util.Stack;

%%

%public
%class DotLexer
%final
%unicode
%char
%line
%column
%ignorecase
%type DotParsedSymbol
%throws DotParseException

%{

    StringBuilder string = new StringBuilder();
    boolean finish = false;
    boolean parameter = false;
    String parameterName = null;
    int tagLevel = 0;

    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public DotLexer() {

    }

    public int yychar() {
        return yychar;
    }

    public int yyline() {
        return yyline + 1;
    }

    private final Stack<DotParsedSymbol> pushedBack = new Stack<>();
   
    public void pushback(DotParsedSymbol symb) {
        pushedBack.push(symb);
        last = null;
    }

    DotParsedSymbol last;
    public DotParsedSymbol lex() throws java.io.IOException, DotParseException{
        DotParsedSymbol ret = null;
        if (!pushedBack.isEmpty()){
            ret = last = pushedBack.pop();
        } else {
            ret = last = yylex();
        }
        System.out.println("LEX:"+ret);
        return ret;
    }

%}


LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]+

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}
TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?

IdentFirst = [a-zA-Z\u0200-\u0377_]
IdentNext = {IdentFirst} | [0-9]

/* identifiers */
Identifier = {IdentFirst}{IdentNext}*

Numeral = [-]?("."[0-9]+ | [0-9]+("."[0-9]*)? )

%state STRING, HTML

%%  
<YYINITIAL> {
  /* keywords */
  "strict"                       { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_KEYWORD_STRICT, yytext()); }
  "graph"                        { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_KEYWORD_GRAPH, yytext()); }
  "digraph"                      { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_KEYWORD_DIGRAPH, yytext()); }
  "node"                         { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_KEYWORD_NODE, yytext()); }
  "edge"                         { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_KEYWORD_EDGE, yytext()); }
  "subgraph"                     { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_KEYWORD_SUBGRAPH, yytext()); }

  ";"                            { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_SEMICOLON, yytext()); }
  ","                            { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_COMMA, yytext()); }
  "{"                            { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_BRACE_OPEN, yytext()); }
  "}"                            { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_BRACE_CLOSE, yytext()); }
  "="                            { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_EQUAL, yytext()); }
  "["                            { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_BRACKET_OPEN, yytext()); }
  "]"                            { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_BRACKET_CLOSE, yytext()); }
  ":"                            { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_COLON, yytext()); }
  "--"                           { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_MINUSMINUS, yytext()); }
  "->"                           { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_ARROW, yytext()); }

  "<"                            {   string.setLength(0);
                                     tagLevel = 1;
                                     yybegin(HTML);
                                 }

  /* comments */
  {Comment}                      { /*ignore*/ }
  
  {LineTerminator}               { yyline++;}

 /* string literal */
  \"                             {
                                     string.setLength(0);
                                     yybegin(STRING);
                                 }  
  {Numeral}                      { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_ID, DotParsedSymbol.IDTYPE_NUMERAL, yytext()); }
  {Identifier}                   { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_ID, DotParsedSymbol.IDTYPE_IDENTIFIER, yytext()); }
  {WhiteSpace}                   { /*ignore*/ }
  .                              { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_INVALID_SYMBOL, yytext()); }

  <<EOF>>                        { return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_EOF, yytext()); }
}

<STRING> {
  \"                             {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_ID, DotParsedSymbol.IDTYPE_DOUBLE_QUOTED, string.toString());
                                 }
  
  /* escape*/
  "\\\""                         { string.append('\"'); } 

  {LineTerminator}               { yyline++; string.append(yytext());}
   .                             { string.append(yytext()); }
}
<HTML> {
    "<"                          { 
                                    tagLevel++; 
                                    string.append(yytext());
                                 }
    ">"                          { 
                                    tagLevel--;                                     
                                    if(tagLevel == 0){
                                        yybegin(YYINITIAL);
                                        return new DotParsedSymbol(yyline(), DotParsedSymbol.TYPE_ID, DotParsedSymbol.IDTYPE_HTML_STRING, string.toString());
                                    }else{
                                        string.append(yytext());
                                    }
                                 }       
    {LineTerminator}             { yyline++; string.append(yytext());}
    .                            {
                                    string.append(yytext());
                                 }
}

/* error fallback */
[^]                              {  }
<<EOF>>                          { return null; }
