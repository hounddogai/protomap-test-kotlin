package com.acme.transport.crewing.availability.availabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityMilestone("code-556", "title-440")
        val view = toAvailabilityMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityMilestoneViews(mutableListOf<AvailabilityMilestone>(item, item)).size, "views")
    }
}
