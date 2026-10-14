package com.acme.transport.safety.incident.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedIncidentMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedIncidentMilestone("code-29", "title-552")
        val view = toBatchedIncidentMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedIncidentMilestoneViews(mutableListOf<BatchedIncidentMilestone>(item, item)).size, "views")
    }
}
