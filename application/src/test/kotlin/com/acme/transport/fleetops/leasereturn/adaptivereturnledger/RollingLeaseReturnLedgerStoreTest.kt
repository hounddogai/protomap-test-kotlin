package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingLeaseReturnLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingLeaseReturnLedgerStore()
        val item = newRollingLeaseReturnLedger("code-714", "label-497")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
