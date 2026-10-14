package com.acme.transport.terminal.dockdoor.doorrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDockDoorWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDockDoorWindowStore()
        val item = newStrictDockDoorWindow("code-650", "name-834")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
