package com.acme.transport.fleetops.leasereturn.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnLedgerStore()
        val item = newLeaseReturnLedger("code-165", "title-425")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
