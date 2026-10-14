package com.acme.transport.lastmile.attempt.attemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAttemptRequestTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientAttemptRequest("code-141", "name-247")
        assertEquals(LenientAttemptRequestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientAttemptRequest("code-141", "name-247")
        try {
            item.moveTo(LenientAttemptRequestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAttemptRequestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientAttemptRequest("code-141", "name-247")
        item.status = LenientAttemptRequestStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientAttemptRequestStatus.PLANNED) }
    }
}
