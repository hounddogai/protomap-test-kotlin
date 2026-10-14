package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalTrailerYardOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalTrailerYardOverride("code-339", "name-578")
        val view = toSeasonalTrailerYardOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalTrailerYardOverrideViews(mutableListOf<SeasonalTrailerYardOverride>(item, item)).size, "views")
    }
}
