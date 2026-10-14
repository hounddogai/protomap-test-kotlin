package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedCameraEventForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedCameraEventForecastStore()
        val item = newWeightedCameraEventForecast("code-362", "title-223")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
