package com.acme.transport.fleetops.utilization.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedUtilizationBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedUtilizationBudgetStore()
        val item = newCappedUtilizationBudget("code-75", "name-393")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
