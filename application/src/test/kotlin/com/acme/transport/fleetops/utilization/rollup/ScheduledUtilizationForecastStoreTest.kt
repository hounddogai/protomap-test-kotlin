package com.acme.transport.fleetops.utilization.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledUtilizationForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledUtilizationForecastStore()
        val item = newScheduledUtilizationForecast("code-135", "name-671")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
