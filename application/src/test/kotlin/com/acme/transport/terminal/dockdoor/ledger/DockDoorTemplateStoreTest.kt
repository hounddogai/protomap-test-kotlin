package com.acme.transport.terminal.dockdoor.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorTemplateStore()
        val item = newDockDoorTemplate("code-754", "label-731")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
