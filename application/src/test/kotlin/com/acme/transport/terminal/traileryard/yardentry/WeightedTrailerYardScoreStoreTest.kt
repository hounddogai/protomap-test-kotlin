package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedTrailerYardScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedTrailerYardScoreStore()
        val item = newWeightedTrailerYardScore("code-352", "title-572")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
