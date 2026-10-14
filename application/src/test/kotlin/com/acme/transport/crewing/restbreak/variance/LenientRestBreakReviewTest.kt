package com.acme.transport.crewing.restbreak.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRestBreakReviewTest {
    @Test
    fun `starts in new`() {
        val item = newLenientRestBreakReview("code-984", "name-761")
        assertEquals(LenientRestBreakReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLenientRestBreakReview("code-984", "name-761")
        try {
            item.moveTo(LenientRestBreakReviewStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRestBreakReviewStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientRestBreakReview("code-984", "name-761")
        item.status = LenientRestBreakReviewStatus.FAILED
        assertFails("move") { item.moveTo(LenientRestBreakReviewStatus.NEW) }
    }
}
