package com.acme.transport.crewing.restbreak.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedRestBreakScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedRestBreakScore("code-31", "title-459")
        val view = toWeightedRestBreakScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedRestBreakScoreViews(mutableListOf<WeightedRestBreakScore>(item, item)).size, "views")
    }
}
