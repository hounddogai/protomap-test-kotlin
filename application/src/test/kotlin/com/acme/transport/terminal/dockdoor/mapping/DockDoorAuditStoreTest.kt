package com.acme.transport.terminal.dockdoor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorAuditStore()
        val item = newDockDoorAudit("code-835", "name-28")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
