/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.lexer.analyzer;

import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Literals;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.lexer.token.Token;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import lombok.RequiredArgsConstructor;

/**
 * 标记器
 * Tokenizer.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class Tokenizer {

    private static final int MYSQL_SPECIAL_COMMENT_BEGIN_SYMBOL_LENGTH = 1;

    private static final int COMMENT_BEGIN_SYMBOL_LENGTH = 2;

    private static final int HINT_BEGIN_SYMBOL_LENGTH = 3;

    private static final int COMMENT_AND_HINT_END_SYMBOL_LENGTH = 2;

    private static final int HEX_BEGIN_SYMBOL_LENGTH = 2;

    private final String input;

    private final Dictionary dictionary;

    private final int offset;

    /**
     * skip whitespace.
     *
     * @return offset after whitespace skipped
     */
    public int skipWhitespace() {
        int length = 0;
        while (CharType.isWhitespace(charAt(offset + length))) {
            length++;
        }
        return offset + length;
    }

    /**
     * skip comment.
     *
     * @return offset after comment skipped
     */
    public int skipComment() {
        char current = charAt(offset);
        char next = charAt(offset + 1);
        if (isSingleLineCommentBegin(current, next)) {
            return skipSingleLineComment(COMMENT_BEGIN_SYMBOL_LENGTH);
        } else if ('#' == current) {
            return skipSingleLineComment(MYSQL_SPECIAL_COMMENT_BEGIN_SYMBOL_LENGTH);
        } else if (isMultipleLineCommentBegin(current, next)) {
            return skipMultiLineComment();
        }
        return offset;
    }

    private boolean isSingleLineCommentBegin(final char ch, final char next) {
        return '/' == ch && '/' == next || '-' == ch && '-' == next;
    }

    private int skipSingleLineComment(final int commentSymbolLength) {
        int length = commentSymbolLength;
        while (!CharType.isEndOfInput(charAt(offset + length)) && '\n' != charAt(offset + length)) {
            length++;
        }
        return offset + length + 1;
    }

    private boolean isMultipleLineCommentBegin(final char ch, final char next) {
        return '/' == ch && '*' == next;
    }

    private int skipMultiLineComment() {
        return untilCommentAndHintTerminateSign(COMMENT_BEGIN_SYMBOL_LENGTH);
    }

    /**
     * skip hint.
     *
     * @return offset after hint skipped
     */
    public int skipHint() {
        return untilCommentAndHintTerminateSign(HINT_BEGIN_SYMBOL_LENGTH);
    }

    private int untilCommentAndHintTerminateSign(final int beginSymbolLength) {
        int length = beginSymbolLength;
        while (!isMultipleLineCommentEnd(charAt(offset + length), charAt(offset + length + 1))) {
            if (CharType.isEndOfInput(charAt(offset + length))) {
                throw new UnterminatedCharException("*/");
            }
            length++;
        }
        return offset + length + COMMENT_AND_HINT_END_SYMBOL_LENGTH;
    }

    private boolean isMultipleLineCommentEnd(final char ch, final char next) {
        return '*' == ch && '/' == next;
    }

    /**
     * 扫描词法变量
     * 例如：
     * select * from commodity.mall_commodity_info where PRODUCT_ID=@id 的 @id
     * select @@version 的 @@version
     * scan variable.
     *
     * @return variable token
     */
    public Token scanVariable() {
        int length = 1;
        if ('@' == charAt(offset + 1)) {
            length++;
        }
        while (isVariableChar(charAt(offset + length))) {
            length++;
        }
        return new Token(Literals.VARIABLE, input.substring(offset, offset + length), offset + length);
    }

    private boolean isVariableChar(final char ch) {
        return isIdentifierChar(ch) || '.' == ch;
    }

    /**
     * 扫描 标识符
     * scan identifier.
     *
     * @return identifier token
     */
    public Token scanIdentifier() {

        // `字段`， 例如 select `id` from table_xx 中的 `id`
        if ('`' == charAt(offset)) {
            int length = getLengthUntilTerminatedChar('`');
            return new Token(Literals.IDENTIFIER, input.substring(offset, offset + length), offset + length);
        }
        if ('"' == charAt(offset)) {
            int length = getLengthUntilTerminatedChar('"');
            return new Token(Literals.IDENTIFIER, input.substring(offset, offset + length), offset + length);
        }
        if ('[' == charAt(offset)) {
            int length = getLengthUntilTerminatedChar(']');
            return new Token(Literals.IDENTIFIER, input.substring(offset, offset + length), offset + length);
        }

        int length = 0;
        while (isIdentifierChar(charAt(offset + length))) {
            length++;
        }
        String literals = input.substring(offset, offset + length);
        if (isAmbiguousIdentifier(literals)) {
            // 模棱两可的， order 或者 group
            //如果空格后面跟着 by 关键字 则TokenType是dictionary里面的KeyWord， 否则Literals.IDENTIFIER
            return new Token(processAmbiguousIdentifier(offset + length, literals), literals, offset + length);
        }
        // TokenType：根据literals 获取 dictionary里面的Keyword， 否则为默认Literals.IDENTIFIER
        return new Token(dictionary.findTokenType(literals, Literals.IDENTIFIER), literals, offset + length);
    }

    /**
     * 计算到结束符的长度
     *
     * @param terminatedChar
     * @return
     * @see #hasEscapeChar,  select * from table_xx as `a``b` 这里连续的"``"并非结束符，如果此时传递的是"'",则会误判，所以这里加了hasEscapeChar判断
     */
    private int getLengthUntilTerminatedChar(final char terminatedChar) {
        int length = 1;
        while (terminatedChar != charAt(offset + length) || hasEscapeChar(terminatedChar, offset + length)) {
            if (offset + length >= input.length()) {
                throw new UnterminatedCharException(terminatedChar);
            }
            if (hasEscapeChar(terminatedChar, offset + length)) {
                length++;
            }
            length++;
        }
        return length + 1;
    }

    /**
     * 是否有 Escape 字符
     *
     * @param charIdentifier
     * @param offset
     * @return
     */
    private boolean hasEscapeChar(final char charIdentifier, final int offset) {
        return charIdentifier == charAt(offset) && charIdentifier == charAt(offset + 1);
    }

    private boolean isIdentifierChar(final char ch) {
        return CharType.isAlphabet(ch) || CharType.isDigital(ch) || '_' == ch || '$' == ch || '#' == ch;
    }

    /**
     * 是否 order 或者 group 关键字标记
     *
     * @param literals
     * @return
     */
    private boolean isAmbiguousIdentifier(final String literals) {
        return DefaultKeyword.ORDER.name().equalsIgnoreCase(literals) || DefaultKeyword.GROUP.name().equalsIgnoreCase(literals);
    }


    /**
     * 处理order 或者 group 关键字标记
     * 如果后面跟着 by 关键字标记，则返回 dictionary获取的Keyword,否则返回Literals.IDENTIFIER
     *
     * @param offset
     * @param literals
     * @return
     */
    private TokenType processAmbiguousIdentifier(final int offset, final String literals) {
        int i = 0;
        while (CharType.isWhitespace(charAt(offset + i))) {
            i++;
        }
        if (DefaultKeyword.BY.name().equalsIgnoreCase(String.valueOf(new char[]{charAt(offset + i), charAt(offset + i + 1)}))) {
            return dictionary.findTokenType(literals);
        }
        return Literals.IDENTIFIER;
    }

    /**
     * scan hex decimal.
     *
     * @return hex decimal token
     */
    public Token scanHexDecimal() {
        int length = HEX_BEGIN_SYMBOL_LENGTH;
        //处理负号
        if ('-' == charAt(offset + length)) {
            length++;
        }
        while (isHex(charAt(offset + length))) {
            length++;
        }
        return new Token(Literals.HEX, input.substring(offset, offset + length), offset + length);
    }

    private boolean isHex(final char ch) {
        return ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f' || CharType.isDigital(ch);
    }

    /**
     * scan number.
     *
     * @return number token
     */
    public Token scanNumber() {
        int length = 0;
        if ('-' == charAt(offset + length)) {
            length++;
        }
        length += getDigitalLength(offset + length);
        boolean isFloat = false;
        if ('.' == charAt(offset + length)) {
            isFloat = true;
            length++;
            length += getDigitalLength(offset + length);
        }
        if (isScientificNotation(offset + length)) {
            isFloat = true;
            length++;
            if ('+' == charAt(offset + length) || '-' == charAt(offset + length)) {
                length++;
            }
            length += getDigitalLength(offset + length);
        }
        if (isBinaryNumber(offset + length)) {
            isFloat = true;
            length++;
        }
        return new Token(isFloat ? Literals.FLOAT : Literals.INT, input.substring(offset, offset + length), offset + length);
    }

    private int getDigitalLength(final int offset) {
        int result = 0;
        while (CharType.isDigital(charAt(offset + result))) {
            result++;
        }
        return result;
    }

    private boolean isScientificNotation(final int offset) {
        char current = charAt(offset);
        return 'e' == current || 'E' == current;
    }

    private boolean isBinaryNumber(final int offset) {
        char current = charAt(offset);
        return 'f' == current || 'F' == current || 'd' == current || 'D' == current;
    }

    /**
     * scan chars.
     *
     * @return chars token
     */
    public Token scanChars() {
        return scanChars(charAt(offset));
    }

    private Token scanChars(final char terminatedChar) {
        int length = getLengthUntilTerminatedChar(terminatedChar);
        return new Token(Literals.CHARS, input.substring(offset + 1, offset + length - 1), offset + length);
    }

    /**
     * scan symbol.
     *
     * @return symbol token
     */
    public Token scanSymbol() {
        int length = 0;
        while (CharType.isSymbol(charAt(offset + length))) {
            length++;
        }
        String literals = input.substring(offset, offset + length);
        Symbol symbol;
        while (null == (symbol = Symbol.literalsOf(literals))) {
            literals = input.substring(offset, offset + --length);
        }
        return new Token(symbol, literals, offset + length);
    }

    private char charAt(final int index) {
        return index >= input.length() ? (char) CharType.EOI : input.charAt(index);
    }
}
