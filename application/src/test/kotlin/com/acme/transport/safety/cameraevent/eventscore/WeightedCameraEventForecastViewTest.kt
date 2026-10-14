package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedCameraEventForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedCameraEventForecast("code-362", "title-223")
        val view = toWeightedCameraEventForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedCameraEventForecastViews(mutableListOf<WeightedCameraEventForecast>(item, item)).size, "views")
    }
}
