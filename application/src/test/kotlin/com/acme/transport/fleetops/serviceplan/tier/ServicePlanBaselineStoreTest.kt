package com.acme.transport.fleetops.serviceplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanBaselineStore()
        val item = newServicePlanBaseline("code-451", "label-848")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
