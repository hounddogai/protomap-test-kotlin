package com.acme.transport.network.hub.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingHubLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingHubLedgerStore()
        val item = newPendingHubLedger("code-943", "label-352")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
