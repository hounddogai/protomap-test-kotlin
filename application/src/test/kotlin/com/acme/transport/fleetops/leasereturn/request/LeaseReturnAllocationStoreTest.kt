package com.acme.transport.fleetops.leasereturn.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnAllocationStore()
        val item = newLeaseReturnAllocation("code-65", "title-914")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
