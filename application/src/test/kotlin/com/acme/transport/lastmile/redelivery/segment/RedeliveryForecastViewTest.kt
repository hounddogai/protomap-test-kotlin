package com.acme.transport.lastmile.redelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryForecast("code-679", "title-633")
        val view = toRedeliveryForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryForecastViews(mutableListOf<RedeliveryForecast>(item, item)).size, "views")
    }
}
