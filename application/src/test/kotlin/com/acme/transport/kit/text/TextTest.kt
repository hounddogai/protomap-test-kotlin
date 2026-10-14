package com.acme.transport.kit.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextTest {
    @Test
    fun `normalizes case and space`() {
        assertEquals("mixed case", normalize("  Mixed Case "), "normalizes case and space")
    }

    @Test
    fun `slugifies names`() {
        assertEquals("spring-sale-2026", slugify(" Spring  Sale 2026 "), "slugifies names")
    }

    @Test
    fun `truncates long values`() {
        assertEquals("abc...", truncate("abcdefghij", 6), "truncates long values")
    }

    @Test
    fun `keeps short values`() {
        assertEquals("abc", truncate("abc", 6), "keeps short values")
    }

    @Test
    fun `masks all but the tail`() {
        assertEquals("************1111", maskTail("4111111111111111", 4), "masks all but the tail")
    }

    @Test
    fun `counts words`() {
        assertEquals(3, wordCount("  one two  three "), "counts words")
    }

    @Test
    fun `fits to a width`() {
        assertEquals("ab  ", fit("ab", 4), "fits to a width")
    }

    @Test
    fun `joins only non empty parts`() {
        assertEquals("a/b", joinNonEmpty(mutableListOf<String>("a", " ", "b"), "/"), "joined")
        assertTrue(containsAny("Late Fee", mutableListOf<String>("fee")), "contains fee")
    }
}
