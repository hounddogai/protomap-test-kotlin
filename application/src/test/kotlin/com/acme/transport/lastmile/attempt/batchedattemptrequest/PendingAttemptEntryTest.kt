package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAttemptEntryTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingAttemptEntry("code-642", "name-548")
        assertEquals(PendingAttemptEntryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newPendingAttemptEntry("code-642", "name-548")
        try {
            item.moveTo(PendingAttemptEntryStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAttemptEntryStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingAttemptEntry("code-642", "name-548")
        item.status = PendingAttemptEntryStatus.STOPPED
        assertFails("move") { item.moveTo(PendingAttemptEntryStatus.IDLE) }
    }
}
