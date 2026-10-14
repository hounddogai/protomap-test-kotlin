package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanBudgetStore()
        val item = newServicePlanBudget("code-779", "label-852")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
