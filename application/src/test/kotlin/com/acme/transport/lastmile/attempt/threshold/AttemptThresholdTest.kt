package com.acme.transport.lastmile.attempt.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptThresholdTest {
    @Test
    fun `starts in planned`() {
        val item = newAttemptThreshold("code-365", "title-255")
        assertEquals(AttemptThresholdStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAttemptThreshold("code-365", "title-255")
        try {
            item.moveTo(AttemptThresholdStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptThresholdStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAttemptThreshold("code-365", "title-255")
        item.status = AttemptThresholdStatus.CANCELLED
        assertFails("move") { item.moveTo(AttemptThresholdStatus.PLANNED) }
    }
}
