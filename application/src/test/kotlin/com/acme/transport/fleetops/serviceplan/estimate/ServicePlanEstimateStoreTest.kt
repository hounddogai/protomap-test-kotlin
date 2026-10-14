package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanEstimateStore()
        val item = newServicePlanEstimate("code-791", "title-88")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
