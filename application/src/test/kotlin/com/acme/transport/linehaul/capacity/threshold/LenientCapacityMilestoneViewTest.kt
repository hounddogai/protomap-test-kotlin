package com.acme.transport.linehaul.capacity.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientCapacityMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientCapacityMilestone("code-362", "label-574")
        val view = toLenientCapacityMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientCapacityMilestoneViews(mutableListOf<LenientCapacityMilestone>(item, item)).size, "views")
    }
}
