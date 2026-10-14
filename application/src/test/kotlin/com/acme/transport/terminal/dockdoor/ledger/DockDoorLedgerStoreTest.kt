package com.acme.transport.terminal.dockdoor.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorLedgerStore()
        val item = newDockDoorLedger("code-6", "label-807")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
