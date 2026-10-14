package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedTrailerYardScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedTrailerYardScore("code-352", "title-572")
        val view = toWeightedTrailerYardScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedTrailerYardScoreViews(mutableListOf<WeightedTrailerYardScore>(item, item)).size, "views")
    }
}
