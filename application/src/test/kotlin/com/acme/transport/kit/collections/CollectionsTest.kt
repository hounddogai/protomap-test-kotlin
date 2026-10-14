package com.acme.transport.kit.collections

import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionsTest {
    @Test
    fun `chunks into batches`() {
        val items = mutableListOf<Int>(1, 2, 3, 4, 5, 6, 7)
        val chunks = chunk(items, 3)
        assertEquals(3, chunks.size, "chunks")
        assertEquals(1, chunks[2].size, "last chunk")
    }

    @Test
    fun `filters and counts`() {
        val items = mutableListOf<Int>(4, 9, 15, 16, 23, 42)
        val even = filter(items) { n: Int -> n % 2 == 0 }
        assertEquals(3, even.size, "even")
        val counts = countBy(items, fun(n: Int): String {
            if (n > 10) {
                return "big"
            }
            return "small"
        })
        assertEquals(4, counts["big"] ?: 0, "big")
    }

    @Test
    fun `drops repeated strings`() {
        assertEquals(2, uniqueStrings(mutableListOf<String>("a", "b", "a")).size, "unique")
    }
}
