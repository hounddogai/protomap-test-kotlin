package com.acme.transport.safety.coaching.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingAssignmentTest {
    @Test
    fun `starts in open`() {
        val item = newCoachingAssignment("code-700", "title-429")
        assertEquals(CoachingAssignmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCoachingAssignment("code-700", "title-429")
        try {
            item.moveTo(CoachingAssignmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingAssignmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCoachingAssignment("code-700", "title-429")
        item.status = CoachingAssignmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(CoachingAssignmentStatus.OPEN) }
    }
}
