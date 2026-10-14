package com.acme.transport.safety.scorecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingScorecardForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingScorecardForecast("code-221", "name-895")
        val view = toPendingScorecardForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingScorecardForecastViews(mutableListOf<PendingScorecardForecast>(item, item)).size, "views")
    }
}
