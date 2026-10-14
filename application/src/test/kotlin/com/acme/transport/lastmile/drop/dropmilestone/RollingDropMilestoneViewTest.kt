package com.acme.transport.lastmile.drop.dropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDropMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDropMilestone("code-974", "label-463")
        val view = toRollingDropMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDropMilestoneViews(mutableListOf<RollingDropMilestone>(item, item)).size, "views")
    }
}
