package com.acme.transport.network.cutoff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCutoffReviewTest {
    @Test
    fun `starts in new`() {
        val item = newPendingCutoffReview("code-196", "label-286")
        assertEquals(PendingCutoffReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPendingCutoffReview("code-196", "label-286")
        try {
            item.moveTo(PendingCutoffReviewStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCutoffReviewStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingCutoffReview("code-196", "label-286")
        item.status = PendingCutoffReviewStatus.FAILED
        assertFails("move") { item.moveTo(PendingCutoffReviewStatus.NEW) }
    }
}
