package com.jpexs.graphs.graphviz.dot.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class DotLexerTest {

    public DotParsedSymbol parse(String source, boolean one, boolean expectLeft) throws DotParseException {
        DotLexer lexer = new DotLexer(new StringReader(source));
        try {
            DotParsedSymbol ret = lexer.lex();
            if (one) {
                if (ret.type == DotParsedSymbol.TYPE_EOF) {
                    Assert.fail("EOF reached instead of symbol");
                }
            }
            DotParsedSymbol next = lexer.lex();
            if (one && expectLeft && next.type == DotParsedSymbol.TYPE_EOF) {
                Assert.fail("Expected unparsed symbol left");
            }
            return ret;
        } catch (IOException ex) {
            Assert.fail("IOException during parse", ex);
            return null;
        }
    }

    public List<DotParsedSymbol> parseAll(String source) throws DotParseException {
        DotLexer lexer = new DotLexer(new StringReader(source));

        List<DotParsedSymbol> ret = new ArrayList<>();
        DotParsedSymbol symbol;
        do {
            try {
                symbol = lexer.lex();
            } catch (IOException ex) {
                Assert.fail("IOException during parse", ex);
                return null;
            }
            ret.add(symbol);
        } while (symbol.type != DotParsedSymbol.TYPE_EOF);
        ret.remove(ret.size() - 1); //remove last EOF
        return ret;
    }

    public DotParsedSymbol parseNoExceptionExpected(String source, boolean one, boolean expectLeft) {
        try {
            return parse(source, one, expectLeft);
        } catch (DotParseException ex) {
            Assert.fail("Exception during parse", ex);
            return null;
        }
    }

    public DotParsedSymbol parseOne(String source) {
        return parseNoExceptionExpected(source, true, false);
    }

    public void parseExpectIdType(String source, int expectedIdType) {
        DotParsedSymbol symbol = parseOne(source);
        if (symbol.type != DotParsedSymbol.TYPE_ID) {
            Assert.fail("ID expected, but " + symbol.getValueAsString() + " found");
        }
        if (symbol.idtype != expectedIdType) {
            Assert.fail("symbol.idtype = " + expectedIdType + " expected, but " + symbol.idtype + " found");
        }
    }

    public void parseExpect(String source, int expectedType) {
        DotParsedSymbol symbol = parseOne(source);
        if (symbol.type != expectedType) {
            Assert.fail("symbol.type = " + expectedType + " expected, but " + symbol.getValueAsString() + " found");
        }
    }

    public void parseNotExpect(String source, int expectedType) {
        List<DotParsedSymbol> symbols;
        try {
            symbols = parseAll(source);
            if (symbols.size() == 1) {
                if (symbols.get(0).type == expectedType) {
                    Assert.fail("symbol.type = " + expectedType + " not expected, but " + symbols.get(0).getValueAsString() + " found");
                }
            }
        } catch (DotParseException ex) {
            //okay
        }
    }

    @DataProvider(name = "provideValidIDs")
    public Object[][] provideValidIDs() {
        return new Object[][]{
            {"myidentifier"},
            {"123"},
            {"\"quoted string\""},
            {"\"quoted with quote inside \\\" end\""},
            {"123.5"},
            {"0"},
            {"-25"},
            {"-24.57"},
            {"0.5"},
            {"-0.1"},
            {"<html>"},
            {"< <sub> aa </sub> aa>"}};
    }

    @DataProvider(name = "provideInvalidIDs")
    public Object[][] provideInvalidIDs() {
        return new Object[][]{
            {"12e"}, {"0a"}, {"-x"}, {".f"}};
    }

    @Test(dataProvider = "provideValidIDs")
    public void testId(String identifier) {
        parseExpect(identifier, DotParsedSymbol.TYPE_ID);
    }

    @DataProvider(name = "provideHTMLIDs")
    public Object[][] provideHTMLIDs() {
        return new Object[][]{
            {"<html>"},
            {"< <sub> aa </sub> aa>"},
            {"< <sub> >"},
            {"<\nnext\n>"}};
    }

    @Test(dataProvider = "provideHTMLIDs")
    public void testHtmlId(String html) {
        parseExpectIdType(html, DotParsedSymbol.IDTYPE_HTML_STRING);
    }

    @Test(dataProvider = "provideInvalidIDs")
    public void testInvalidId(String identifier) {
        parseNotExpect(identifier, DotParsedSymbol.TYPE_ID);
    }

    @DataProvider(name = "provideKeywordAndType")
    public Object[][] provideKeywordAndType() {
        return new Object[][]{
            {"graph", DotParsedSymbol.TYPE_KEYWORD_GRAPH},
            {"digraph", DotParsedSymbol.TYPE_KEYWORD_DIGRAPH},
            {"edge", DotParsedSymbol.TYPE_KEYWORD_EDGE},
            {"node", DotParsedSymbol.TYPE_KEYWORD_NODE},
            {"strict", DotParsedSymbol.TYPE_KEYWORD_STRICT},
            {"subgraph", DotParsedSymbol.TYPE_KEYWORD_SUBGRAPH},};
    }

    @DataProvider(name = "provideOperators")
    public Object[][] provideOperators() {
        return new Object[][]{
            {"->", DotParsedSymbol.TYPE_ARROW},
            {"--", DotParsedSymbol.TYPE_MINUSMINUS},
            {";", DotParsedSymbol.TYPE_SEMICOLON},
            {"}", DotParsedSymbol.TYPE_BRACE_CLOSE},
            {"{", DotParsedSymbol.TYPE_BRACE_OPEN},
            {"]", DotParsedSymbol.TYPE_BRACKET_CLOSE},
            {"[", DotParsedSymbol.TYPE_BRACKET_OPEN},
            {":", DotParsedSymbol.TYPE_COLON},
            {",", DotParsedSymbol.TYPE_COMMA},
            {"=", DotParsedSymbol.TYPE_EQUAL},};
    }

    @Test(dataProvider = "provideKeywordAndType")
    public void testKeywords(String keyword, int type) {
        parseExpect(keyword, type);
    }

    @Test(dataProvider = "provideOperators")
    public void testOperators(String operator, int type) {
        parseExpect(operator, type);
    }

    @Test
    public void testEof() {
        int parsedType = parseNoExceptionExpected("", false, false).type;
        Assert.assertEquals(parsedType, DotParsedSymbol.TYPE_EOF, "Must be EOF");
    }

    @DataProvider(name = "provideInvalidSymbols")
    public Object[][] provideInvalidSymbols() {
        return new Object[][]{
            {"!"},
            {"-"},
            {">"},};
    }

    @Test(dataProvider = "provideInvalidSymbols")
    public void testInvalid(String symbol) {
        parseExpect(symbol, DotParsedSymbol.TYPE_INVALID_SYMBOL);
    }

    @Test
    public void testNewLine() throws DotParseException {
        List<DotParsedSymbol> symbols = parseAll("a\nb");
        Assert.assertEquals(symbols.size(), 2);
        Assert.assertSame(symbols.get(0).type, DotParsedSymbol.TYPE_ID);
        Assert.assertSame(symbols.get(1).type, DotParsedSymbol.TYPE_ID);
    }

}
