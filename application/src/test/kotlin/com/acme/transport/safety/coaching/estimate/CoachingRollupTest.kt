package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newCoachingRollup("code-328", "title-210")
        assertEquals(CoachingRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newCoachingRollup("code-328", "title-210")
        try {
            item.moveTo(CoachingRollupStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingRollupStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCoachingRollup("code-328", "title-210")
        item.status = CoachingRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(CoachingRollupStatus.PLANNED) }
    }
}
