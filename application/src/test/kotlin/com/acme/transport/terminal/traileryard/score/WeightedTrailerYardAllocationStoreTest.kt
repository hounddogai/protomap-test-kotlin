package com.acme.transport.terminal.traileryard.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedTrailerYardAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedTrailerYardAllocationStore()
        val item = newWeightedTrailerYardAllocation("code-962", "title-477")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
