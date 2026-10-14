package com.acme.transport.fleetops.leasereturn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnAdjustmentStore()
        val item = newLeaseReturnAdjustment("code-724", "name-467")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
