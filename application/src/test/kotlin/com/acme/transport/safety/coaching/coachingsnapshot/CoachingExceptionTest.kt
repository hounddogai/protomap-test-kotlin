package com.acme.transport.safety.coaching.coachingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingExceptionTest {
    @Test
    fun `starts in open`() {
        val item = newCoachingException("code-335", "label-695")
        assertEquals(CoachingExceptionStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCoachingException("code-335", "label-695")
        try {
            item.moveTo(CoachingExceptionStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingExceptionStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCoachingException("code-335", "label-695")
        item.status = CoachingExceptionStatus.ARCHIVED
        assertFails("move") { item.moveTo(CoachingExceptionStatus.OPEN) }
    }
}
