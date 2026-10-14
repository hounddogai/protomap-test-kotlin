package com.acme.transport.terminal.dwell.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDwellForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDwellForecast("code-628", "name-503")
        val view = toLenientDwellForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDwellForecastViews(mutableListOf<LenientDwellForecast>(item, item)).size, "views")
    }
}
