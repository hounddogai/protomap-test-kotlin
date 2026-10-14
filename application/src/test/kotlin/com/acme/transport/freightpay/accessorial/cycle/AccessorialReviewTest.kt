package com.acme.transport.freightpay.accessorial.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialReviewTest {
    @Test
    fun `starts in draft`() {
        val item = newAccessorialReview("code-120", "title-727")
        assertEquals(AccessorialReviewStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAccessorialReview("code-120", "title-727")
        try {
            item.moveTo(AccessorialReviewStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialReviewStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAccessorialReview("code-120", "title-727")
        item.status = AccessorialReviewStatus.CLOSED
        assertFails("move") { item.moveTo(AccessorialReviewStatus.DRAFT) }
    }
}
