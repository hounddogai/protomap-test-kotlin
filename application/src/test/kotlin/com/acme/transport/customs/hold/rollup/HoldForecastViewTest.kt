package com.acme.transport.customs.hold.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldForecast("code-373", "name-147")
        val view = toHoldForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldForecastViews(mutableListOf<HoldForecast>(item, item)).size, "views")
    }
}
