package com.acme.transport.crewing.availability.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledAvailabilityForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledAvailabilityForecastStore()
        val item = newScheduledAvailabilityForecast("code-861", "title-47")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
