package com.acme.transport.crewing.availability.availabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newAvailabilityMilestone("code-556", "title-440")
        assertEquals(AvailabilityMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newAvailabilityMilestone("code-556", "title-440")
        try {
            item.moveTo(AvailabilityMilestoneStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityMilestoneStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAvailabilityMilestone("code-556", "title-440")
        item.status = AvailabilityMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(AvailabilityMilestoneStatus.PLANNED) }
    }
}
