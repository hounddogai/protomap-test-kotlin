package com.acme.transport.terminal.dockdoor.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorGroupStore()
        val item = newDockDoorGroup("code-871", "label-506")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
