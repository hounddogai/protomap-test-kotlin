package com.acme.transport.safety.coaching.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingThresholdTest {
    @Test
    fun `starts in planned`() {
        val item = newCoachingThreshold("code-303", "name-914")
        assertEquals(CoachingThresholdStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newCoachingThreshold("code-303", "name-914")
        try {
            item.moveTo(CoachingThresholdStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingThresholdStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCoachingThreshold("code-303", "name-914")
        item.status = CoachingThresholdStatus.CANCELLED
        assertFails("move") { item.moveTo(CoachingThresholdStatus.PLANNED) }
    }
}
