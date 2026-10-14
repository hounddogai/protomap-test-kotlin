package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAttemptRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingAttemptRequest("code-140", "label-438")
        assertEquals(PendingAttemptRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newPendingAttemptRequest("code-140", "label-438")
        try {
            item.moveTo(PendingAttemptRequestStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAttemptRequestStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingAttemptRequest("code-140", "label-438")
        item.status = PendingAttemptRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingAttemptRequestStatus.REQUESTED) }
    }
}
