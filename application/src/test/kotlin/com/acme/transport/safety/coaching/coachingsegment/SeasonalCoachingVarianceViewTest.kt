package com.acme.transport.safety.coaching.coachingsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalCoachingVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalCoachingVariance("code-335", "label-154")
        val view = toSeasonalCoachingVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalCoachingVarianceViews(mutableListOf<SeasonalCoachingVariance>(item, item)).size, "views")
    }
}
