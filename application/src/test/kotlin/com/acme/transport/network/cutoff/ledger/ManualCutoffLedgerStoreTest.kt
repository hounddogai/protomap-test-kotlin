package com.acme.transport.network.cutoff.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCutoffLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCutoffLedgerStore()
        val item = newManualCutoffLedger("code-745", "label-749")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
