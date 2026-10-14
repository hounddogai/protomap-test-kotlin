package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRedeliveryForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRedeliveryForecast("code-367", "label-688")
        val view = toStrictRedeliveryForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRedeliveryForecastViews(mutableListOf<StrictRedeliveryForecast>(item, item)).size, "views")
    }
}
