package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedServicePlanMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedServicePlanMilestoneStore()
        val item = newWeightedServicePlanMilestone("code-838", "label-22")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
