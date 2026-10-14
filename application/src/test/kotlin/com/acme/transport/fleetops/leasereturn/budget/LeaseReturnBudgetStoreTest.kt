package com.acme.transport.fleetops.leasereturn.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnBudgetStore()
        val item = newLeaseReturnBudget("code-827", "title-214")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
