package com.acme.transport.fleetops.leasereturn.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryLeaseReturnBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryLeaseReturnBudgetStore()
        val item = newPrimaryLeaseReturnBudget("code-597", "label-197")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
