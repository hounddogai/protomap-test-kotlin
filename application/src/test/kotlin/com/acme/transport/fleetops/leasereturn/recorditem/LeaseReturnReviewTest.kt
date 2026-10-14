package com.acme.transport.fleetops.leasereturn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newLeaseReturnReview("code-312", "title-798")
        assertEquals(LeaseReturnReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLeaseReturnReview("code-312", "title-798")
        try {
            item.moveTo(LeaseReturnReviewStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnReviewStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLeaseReturnReview("code-312", "title-798")
        item.status = LeaseReturnReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LeaseReturnReviewStatus.PROPOSED) }
    }
}
