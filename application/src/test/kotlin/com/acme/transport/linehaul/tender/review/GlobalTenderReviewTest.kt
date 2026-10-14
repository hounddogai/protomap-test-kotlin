package com.acme.transport.linehaul.tender.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTenderReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalTenderReview("code-393", "title-911")
        assertEquals(GlobalTenderReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newGlobalTenderReview("code-393", "title-911")
        try {
            item.moveTo(GlobalTenderReviewStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTenderReviewStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalTenderReview("code-393", "title-911")
        item.status = GlobalTenderReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalTenderReviewStatus.PROPOSED) }
    }
}
