package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledServicePlanMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledServicePlanMetricStore()
        val item = newScheduledServicePlanMetric("code-533", "title-333")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
