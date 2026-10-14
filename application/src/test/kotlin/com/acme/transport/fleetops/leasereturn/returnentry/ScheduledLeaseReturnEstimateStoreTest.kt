package com.acme.transport.fleetops.leasereturn.returnentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledLeaseReturnEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledLeaseReturnEstimateStore()
        val item = newScheduledLeaseReturnEstimate("code-556", "label-727")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
