package com.acme.transport.linehaul.capacity.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityMilestone("code-711", "name-936")
        val view = toCapacityMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityMilestoneViews(mutableListOf<CapacityMilestone>(item, item)).size, "views")
    }
}
