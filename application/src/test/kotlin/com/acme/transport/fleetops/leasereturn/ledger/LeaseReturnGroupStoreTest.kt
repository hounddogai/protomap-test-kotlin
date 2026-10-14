package com.acme.transport.fleetops.leasereturn.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnGroupStore()
        val item = newLeaseReturnGroup("code-625", "label-336")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
