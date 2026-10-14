package com.acme.transport.terminal.traileryard.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedTrailerYardTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedTrailerYardTierStore()
        val item = newWeightedTrailerYardTier("code-220", "name-362")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
