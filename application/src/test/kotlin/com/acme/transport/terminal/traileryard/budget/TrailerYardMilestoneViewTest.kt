package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardMilestone("code-2", "name-593")
        val view = toTrailerYardMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardMilestoneViews(mutableListOf<TrailerYardMilestone>(item, item)).size, "views")
    }
}
