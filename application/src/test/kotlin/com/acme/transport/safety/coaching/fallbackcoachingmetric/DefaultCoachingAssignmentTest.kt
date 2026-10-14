package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCoachingAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultCoachingAssignment("code-851", "label-562")
        assertEquals(DefaultCoachingAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDefaultCoachingAssignment("code-851", "label-562")
        try {
            item.moveTo(DefaultCoachingAssignmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCoachingAssignmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultCoachingAssignment("code-851", "label-562")
        item.status = DefaultCoachingAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultCoachingAssignmentStatus.IDLE) }
    }
}
