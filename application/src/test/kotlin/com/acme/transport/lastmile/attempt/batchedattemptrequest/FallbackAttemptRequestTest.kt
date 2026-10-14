package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAttemptRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackAttemptRequest("code-934", "label-85")
        assertEquals(FallbackAttemptRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newFallbackAttemptRequest("code-934", "label-85")
        try {
            item.moveTo(FallbackAttemptRequestStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAttemptRequestStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackAttemptRequest("code-934", "label-85")
        item.status = FallbackAttemptRequestStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackAttemptRequestStatus.IDLE) }
    }
}
