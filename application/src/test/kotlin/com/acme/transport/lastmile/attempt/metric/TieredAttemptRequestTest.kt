package com.acme.transport.lastmile.attempt.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAttemptRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredAttemptRequest("code-576", "label-813")
        assertEquals(TieredAttemptRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredAttemptRequest("code-576", "label-813")
        try {
            item.moveTo(TieredAttemptRequestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAttemptRequestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredAttemptRequest("code-576", "label-813")
        item.status = TieredAttemptRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredAttemptRequestStatus.PROPOSED) }
    }
}
