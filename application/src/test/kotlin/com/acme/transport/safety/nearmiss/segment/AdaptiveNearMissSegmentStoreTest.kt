package com.acme.transport.safety.nearmiss.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveNearMissSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveNearMissSegmentStore()
        val item = newAdaptiveNearMissSegment("code-763", "title-765")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
