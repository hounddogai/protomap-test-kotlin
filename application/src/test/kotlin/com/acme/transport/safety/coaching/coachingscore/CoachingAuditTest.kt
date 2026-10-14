package com.acme.transport.safety.coaching.coachingscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newCoachingAudit("code-355", "title-427")
        assertEquals(CoachingAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newCoachingAudit("code-355", "title-427")
        try {
            item.moveTo(CoachingAuditStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingAuditStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCoachingAudit("code-355", "title-427")
        item.status = CoachingAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(CoachingAuditStatus.PLANNED) }
    }
}
