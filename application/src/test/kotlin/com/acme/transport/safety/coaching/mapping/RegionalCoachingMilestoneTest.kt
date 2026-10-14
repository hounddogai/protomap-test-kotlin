package com.acme.transport.safety.coaching.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCoachingMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalCoachingMilestone("code-779", "name-924")
        assertEquals(RegionalCoachingMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRegionalCoachingMilestone("code-779", "name-924")
        try {
            item.moveTo(RegionalCoachingMilestoneStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCoachingMilestoneStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalCoachingMilestone("code-779", "name-924")
        item.status = RegionalCoachingMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalCoachingMilestoneStatus.PLANNED) }
    }
}
