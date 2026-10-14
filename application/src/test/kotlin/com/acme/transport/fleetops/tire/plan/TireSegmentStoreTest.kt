package com.acme.transport.fleetops.tire.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireSegmentStore()
        val item = newTireSegment("code-517", "label-373")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
