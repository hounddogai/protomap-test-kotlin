package com.acme.transport.freightpay.accessorial.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAccessorialReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientAccessorialReview("code-584", "name-434")
        assertEquals(LenientAccessorialReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLenientAccessorialReview("code-584", "name-434")
        try {
            item.moveTo(LenientAccessorialReviewStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAccessorialReviewStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientAccessorialReview("code-584", "name-434")
        item.status = LenientAccessorialReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientAccessorialReviewStatus.PROPOSED) }
    }
}
