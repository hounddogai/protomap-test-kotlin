package com.acme.transport.terminal.dockdoor.doorrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDockDoorRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDockDoorRecordStore()
        val item = newScheduledDockDoorRecord("code-858", "name-439")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
