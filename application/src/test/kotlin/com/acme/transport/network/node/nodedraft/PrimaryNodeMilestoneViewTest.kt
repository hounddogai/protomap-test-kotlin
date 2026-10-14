package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryNodeMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryNodeMilestone("code-282", "name-352")
        val view = toPrimaryNodeMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryNodeMilestoneViews(mutableListOf<PrimaryNodeMilestone>(item, item)).size, "views")
    }
}
