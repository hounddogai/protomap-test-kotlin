package com.acme.transport.fleetops.leasereturn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnMilestoneStore()
        val item = newLeaseReturnMilestone("code-426", "name-413")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
