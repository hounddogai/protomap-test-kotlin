package com.acme.transport.terminal.dockdoor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorProfileStore()
        val item = newDockDoorProfile("code-712", "label-544")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
