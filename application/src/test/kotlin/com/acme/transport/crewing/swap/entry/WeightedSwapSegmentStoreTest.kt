package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedSwapSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedSwapSegmentStore()
        val item = newWeightedSwapSegment("code-976", "name-175")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
