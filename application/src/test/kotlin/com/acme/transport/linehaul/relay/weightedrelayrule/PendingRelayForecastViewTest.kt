package com.acme.transport.linehaul.relay.weightedrelayrule

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingRelayForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingRelayForecast("code-127", "label-433")
        val view = toPendingRelayForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingRelayForecastViews(mutableListOf<PendingRelayForecast>(item, item)).size, "views")
    }
}
