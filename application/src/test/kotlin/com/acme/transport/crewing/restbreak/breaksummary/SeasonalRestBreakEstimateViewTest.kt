package com.acme.transport.crewing.restbreak.breaksummary

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRestBreakEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRestBreakEstimate("code-18", "name-799")
        val view = toSeasonalRestBreakEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRestBreakEstimateViews(mutableListOf<SeasonalRestBreakEstimate>(item, item)).size, "views")
    }
}
