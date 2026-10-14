package com.acme.transport.network.transittime.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTransitTimeSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTransitTimeSegmentStore()
        val item = newGlobalTransitTimeSegment("code-607", "label-228")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
