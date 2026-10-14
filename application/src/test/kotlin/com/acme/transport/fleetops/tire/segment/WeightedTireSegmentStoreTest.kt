package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedTireSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedTireSegmentStore()
        val item = newWeightedTireSegment("code-266", "name-615")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
