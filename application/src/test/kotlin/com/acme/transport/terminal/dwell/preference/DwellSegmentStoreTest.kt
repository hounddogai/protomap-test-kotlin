package com.acme.transport.terminal.dwell.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellSegmentStore()
        val item = newDwellSegment("code-190", "label-62")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
