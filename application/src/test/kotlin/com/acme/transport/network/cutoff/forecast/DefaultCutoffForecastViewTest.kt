package com.acme.transport.network.cutoff.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultCutoffForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultCutoffForecast("code-98", "label-565")
        val view = toDefaultCutoffForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultCutoffForecastViews(mutableListOf<DefaultCutoffForecast>(item, item)).size, "views")
    }
}
