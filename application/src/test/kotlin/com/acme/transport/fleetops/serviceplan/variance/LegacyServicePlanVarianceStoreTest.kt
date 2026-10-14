package com.acme.transport.fleetops.serviceplan.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyServicePlanVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyServicePlanVarianceStore()
        val item = newLegacyServicePlanVariance("code-286", "name-955")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
