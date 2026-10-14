package com.acme.transport.fleetops.leasereturn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalLeaseReturnAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalLeaseReturnAdjustmentStore()
        val item = newGlobalLeaseReturnAdjustment("code-474", "name-970")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
