package com.acme.transport.terminal.dockdoor.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorWindowStore()
        val item = newDockDoorWindow("code-581", "name-804")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
