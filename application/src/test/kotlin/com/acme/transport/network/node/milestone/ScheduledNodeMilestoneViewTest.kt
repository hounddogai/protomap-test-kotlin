package com.acme.transport.network.node.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledNodeMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledNodeMilestone("code-260", "title-40")
        val view = toScheduledNodeMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledNodeMilestoneViews(mutableListOf<ScheduledNodeMilestone>(item, item)).size, "views")
    }
}
