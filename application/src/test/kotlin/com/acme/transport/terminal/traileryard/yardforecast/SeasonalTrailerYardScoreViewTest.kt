package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalTrailerYardScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalTrailerYardScore("code-456", "title-798")
        val view = toSeasonalTrailerYardScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalTrailerYardScoreViews(mutableListOf<SeasonalTrailerYardScore>(item, item)).size, "views")
    }
}
