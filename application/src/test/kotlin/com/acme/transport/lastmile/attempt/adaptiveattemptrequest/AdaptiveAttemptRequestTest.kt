package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAttemptRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveAttemptRequest("code-854", "label-82")
        assertEquals(AdaptiveAttemptRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAdaptiveAttemptRequest("code-854", "label-82")
        try {
            item.moveTo(AdaptiveAttemptRequestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAttemptRequestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveAttemptRequest("code-854", "label-82")
        item.status = AdaptiveAttemptRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveAttemptRequestStatus.REQUESTED) }
    }
}
