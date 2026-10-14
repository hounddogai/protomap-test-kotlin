package com.acme.transport.customs.broker.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerForecast("code-719", "name-620")
        val view = toBrokerForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerForecastViews(mutableListOf<BrokerForecast>(item, item)).size, "views")
    }
}
