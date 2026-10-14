package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyCrossDockLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyCrossDockLedgerStore()
        val item = newLegacyCrossDockLedger("code-606", "name-885")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
