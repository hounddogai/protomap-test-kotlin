package com.acme.transport.network.transittime.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryTransitTimeSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryTransitTimeSegmentStore()
        val item = newPrimaryTransitTimeSegment("code-479", "label-437")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
