package com.acme.transport.crewing.roster.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalRosterSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalRosterSegmentStore()
        val item = newRegionalRosterSegment("code-184", "name-594")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
