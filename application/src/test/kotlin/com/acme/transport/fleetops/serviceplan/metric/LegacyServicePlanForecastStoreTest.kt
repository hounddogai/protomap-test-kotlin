package com.acme.transport.fleetops.serviceplan.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyServicePlanForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyServicePlanForecastStore()
        val item = newLegacyServicePlanForecast("code-548", "title-872")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
