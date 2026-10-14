package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackServiceAreaForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackServiceAreaForecast("code-408", "title-678")
        val view = toFallbackServiceAreaForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackServiceAreaForecastViews(mutableListOf<FallbackServiceAreaForecast>(item, item)).size, "views")
    }
}
