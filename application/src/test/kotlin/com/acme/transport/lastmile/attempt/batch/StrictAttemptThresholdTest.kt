package com.acme.transport.lastmile.attempt.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictAttemptThresholdTest {
    @Test
    fun `starts in open`() {
        val item = newStrictAttemptThreshold("code-848", "label-737")
        assertEquals(StrictAttemptThresholdStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newStrictAttemptThreshold("code-848", "label-737")
        try {
            item.moveTo(StrictAttemptThresholdStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictAttemptThresholdStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictAttemptThreshold("code-848", "label-737")
        item.status = StrictAttemptThresholdStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictAttemptThresholdStatus.OPEN) }
    }
}
