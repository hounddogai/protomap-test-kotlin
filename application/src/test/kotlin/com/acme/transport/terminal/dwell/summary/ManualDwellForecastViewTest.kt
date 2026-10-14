package com.acme.transport.terminal.dwell.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDwellForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDwellForecast("code-500", "title-369")
        val view = toManualDwellForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDwellForecastViews(mutableListOf<ManualDwellForecast>(item, item)).size, "views")
    }
}
