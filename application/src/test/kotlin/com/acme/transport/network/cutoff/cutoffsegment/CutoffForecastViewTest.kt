package com.acme.transport.network.cutoff.cutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffForecast("code-679", "title-620")
        val view = toCutoffForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffForecastViews(mutableListOf<CutoffForecast>(item, item)).size, "views")
    }
}
