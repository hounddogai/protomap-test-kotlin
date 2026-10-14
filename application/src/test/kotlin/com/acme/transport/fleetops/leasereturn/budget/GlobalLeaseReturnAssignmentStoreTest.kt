package com.acme.transport.fleetops.leasereturn.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalLeaseReturnAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalLeaseReturnAssignmentStore()
        val item = newGlobalLeaseReturnAssignment("code-142", "title-578")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
