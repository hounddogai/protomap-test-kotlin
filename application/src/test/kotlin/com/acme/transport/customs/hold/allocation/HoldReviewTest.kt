package com.acme.transport.customs.hold.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldReviewTest {
    @Test
    fun `starts in new`() {
        val item = newHoldReview("code-453", "title-339")
        assertEquals(HoldReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newHoldReview("code-453", "title-339")
        try {
            item.moveTo(HoldReviewStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldReviewStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newHoldReview("code-453", "title-339")
        item.status = HoldReviewStatus.FAILED
        assertFails("move") { item.moveTo(HoldReviewStatus.NEW) }
    }
}
