package com.acme.transport.safety.scorecard.scorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultScorecardForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultScorecardForecast("code-786", "name-524")
        val view = toDefaultScorecardForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultScorecardForecastViews(mutableListOf<DefaultScorecardForecast>(item, item)).size, "views")
    }
}
