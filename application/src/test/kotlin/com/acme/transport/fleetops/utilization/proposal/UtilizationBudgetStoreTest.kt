package com.acme.transport.fleetops.utilization.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationBudgetStore()
        val item = newUtilizationBudget("code-116", "name-337")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
