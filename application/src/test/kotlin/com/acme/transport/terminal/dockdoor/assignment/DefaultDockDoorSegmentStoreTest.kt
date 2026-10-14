package com.acme.transport.terminal.dockdoor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultDockDoorSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultDockDoorSegmentStore()
        val item = newDefaultDockDoorSegment("code-932", "title-287")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
