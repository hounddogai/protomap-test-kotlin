package com.acme.transport.customs.hold.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedHoldSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedHoldSegment("code-171", "title-315")
        val view = toWeightedHoldSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedHoldSegmentViews(mutableListOf<WeightedHoldSegment>(item, item)).size, "views")
    }
}
