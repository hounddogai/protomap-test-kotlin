package com.acme.transport.safety.incident.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedIncidentMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedIncidentMilestone("code-29", "title-552")
        assertEquals(BatchedIncidentMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newBatchedIncidentMilestone("code-29", "title-552")
        try {
            item.moveTo(BatchedIncidentMilestoneStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedIncidentMilestoneStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedIncidentMilestone("code-29", "title-552")
        item.status = BatchedIncidentMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedIncidentMilestoneStatus.PLANNED) }
    }
}
