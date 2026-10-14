package com.acme.transport.lastmile.drop.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class DropMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropMilestone("code-131", "name-608")
        val view = toDropMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropMilestoneViews(mutableListOf<DropMilestone>(item, item)).size, "views")
    }
}
