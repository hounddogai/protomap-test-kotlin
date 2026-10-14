package com.acme.transport.safety.cameraevent.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCameraEventMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledCameraEventMilestone("code-673", "name-355")
        assertEquals(ScheduledCameraEventMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newScheduledCameraEventMilestone("code-673", "name-355")
        try {
            item.moveTo(ScheduledCameraEventMilestoneStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCameraEventMilestoneStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledCameraEventMilestone("code-673", "name-355")
        item.status = ScheduledCameraEventMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledCameraEventMilestoneStatus.REQUESTED) }
    }
}
