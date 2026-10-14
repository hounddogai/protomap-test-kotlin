package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledLeaseReturnLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledLeaseReturnLedgerStore()
        val item = newScheduledLeaseReturnLedger("code-315", "label-916")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
