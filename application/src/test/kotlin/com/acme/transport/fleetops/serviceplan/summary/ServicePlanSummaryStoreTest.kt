package com.acme.transport.fleetops.serviceplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanSummaryStore()
        val item = newServicePlanSummary("code-192", "name-787")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
