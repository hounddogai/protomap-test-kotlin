package com.acme.transport.linehaul.backhaul.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalBackhaulLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalBackhaulLedgerStore()
        val item = newGlobalBackhaulLedger("code-501", "name-553")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
