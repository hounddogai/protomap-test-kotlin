package com.acme.transport.customs.clearance.clearancemilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingClearanceMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingClearanceMilestone("code-390", "label-34")
        val view = toRollingClearanceMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingClearanceMilestoneViews(mutableListOf<RollingClearanceMilestone>(item, item)).size, "views")
    }
}
