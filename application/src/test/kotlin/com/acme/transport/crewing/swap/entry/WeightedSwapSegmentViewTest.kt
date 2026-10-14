package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedSwapSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedSwapSegment("code-976", "name-175")
        val view = toWeightedSwapSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedSwapSegmentViews(mutableListOf<WeightedSwapSegment>(item, item)).size, "views")
    }
}
