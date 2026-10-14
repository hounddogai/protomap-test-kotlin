package com.acme.transport.fleetops.leasereturn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnPlanStore()
        val item = newLeaseReturnPlan("code-10", "title-872")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
