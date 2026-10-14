package com.acme.transport.lastmile.attempt.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAttemptSlotTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledAttemptSlot("code-201", "label-255")
        assertEquals(ScheduledAttemptSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledAttemptSlot("code-201", "label-255")
        try {
            item.moveTo(ScheduledAttemptSlotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAttemptSlotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledAttemptSlot("code-201", "label-255")
        item.status = ScheduledAttemptSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledAttemptSlotStatus.OPEN) }
    }
}
