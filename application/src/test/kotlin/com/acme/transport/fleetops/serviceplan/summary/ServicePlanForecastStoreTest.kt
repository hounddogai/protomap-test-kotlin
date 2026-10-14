package com.acme.transport.fleetops.serviceplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanForecastStore()
        val item = newServicePlanForecast("code-744", "label-44")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
