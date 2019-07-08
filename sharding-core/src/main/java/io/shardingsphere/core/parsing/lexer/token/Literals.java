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

package io.shardingsphere.core.parsing.lexer.token;

/**
 * 词法字面量标记
 * Literals token.
 *
 * @author zhangliang
 */
public enum Literals implements TokenType {

    /**
     * Integer类型
     */
    INT,
    /**
     * Float类型
     */
    FLOAT,
    /**
     * 16进制
     */
    HEX,
    /**
     * 字符串
     */
    CHARS,
    /**
     * 词法关键字
     */
    IDENTIFIER,
    /**
     * 变量
     */
    VARIABLE
}
