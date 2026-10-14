package com.acme.transport.network.cutoff.regionalcutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCutoffSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingCutoffSegment("code-46", "name-155")
        assertEquals(PendingCutoffSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPendingCutoffSegment("code-46", "name-155")
        try {
            item.moveTo(PendingCutoffSegmentStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCutoffSegmentStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingCutoffSegment("code-46", "name-155")
        item.status = PendingCutoffSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingCutoffSegmentStatus.PROPOSED) }
    }
}
