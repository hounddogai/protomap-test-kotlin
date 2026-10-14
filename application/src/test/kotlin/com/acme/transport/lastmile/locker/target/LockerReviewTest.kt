package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newLockerReview("code-955", "label-117")
        assertEquals(LockerReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLockerReview("code-955", "label-117")
        try {
            item.moveTo(LockerReviewStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerReviewStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLockerReview("code-955", "label-117")
        item.status = LockerReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LockerReviewStatus.PROPOSED) }
    }
}
