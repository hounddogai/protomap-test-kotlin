package com.acme.transport.emissions.idling.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingMilestone("code-775", "title-393")
        val view = toIdlingMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingMilestoneViews(mutableListOf<IdlingMilestone>(item, item)).size, "views")
    }
}
