package com.acme.transport.safety.coaching.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newCoachingPolicy("code-455", "name-837")
        assertEquals(CoachingPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newCoachingPolicy("code-455", "name-837")
        try {
            item.moveTo(CoachingPolicyStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingPolicyStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCoachingPolicy("code-455", "name-837")
        item.status = CoachingPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(CoachingPolicyStatus.IDLE) }
    }
}
