package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityForecast("code-230", "name-82")
        val view = toCapacityForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityForecastViews(mutableListOf<CapacityForecast>(item, item)).size, "views")
    }
}
