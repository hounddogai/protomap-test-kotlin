package com.acme.transport.safety.coaching.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newCoachingTarget("code-336", "title-852")
        assertEquals(CoachingTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newCoachingTarget("code-336", "title-852")
        try {
            item.moveTo(CoachingTargetStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingTargetStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCoachingTarget("code-336", "title-852")
        item.status = CoachingTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(CoachingTargetStatus.PLANNED) }
    }
}
