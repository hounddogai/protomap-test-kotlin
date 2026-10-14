package com.acme.transport.crewing.roster.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterSegmentStore()
        val item = newRosterSegment("code-22", "label-111")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
