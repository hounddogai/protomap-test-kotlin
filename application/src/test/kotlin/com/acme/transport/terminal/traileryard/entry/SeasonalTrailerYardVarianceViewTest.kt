package com.acme.transport.terminal.traileryard.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalTrailerYardVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalTrailerYardVariance("code-878", "name-676")
        val view = toSeasonalTrailerYardVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalTrailerYardVarianceViews(mutableListOf<SeasonalTrailerYardVariance>(item, item)).size, "views")
    }
}
