package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRestBreakVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRestBreakVariance("code-581", "title-983")
        val view = toSeasonalRestBreakVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRestBreakVarianceViews(mutableListOf<SeasonalRestBreakVariance>(item, item)).size, "views")
    }
}
