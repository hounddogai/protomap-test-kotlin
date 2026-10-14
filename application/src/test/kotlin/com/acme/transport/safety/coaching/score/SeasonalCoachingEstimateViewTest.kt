package com.acme.transport.safety.coaching.score

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalCoachingEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalCoachingEstimate("code-479", "name-225")
        val view = toSeasonalCoachingEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalCoachingEstimateViews(mutableListOf<SeasonalCoachingEstimate>(item, item)).size, "views")
    }
}
