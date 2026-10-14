package com.acme.transport.lastmile.drop.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDropForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDropForecast("code-693", "title-736")
        val view = toFallbackDropForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDropForecastViews(mutableListOf<FallbackDropForecast>(item, item)).size, "views")
    }
}
