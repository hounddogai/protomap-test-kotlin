package com.acme.transport.lastmile.locker.lockertarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLockerReviewTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledLockerReview("code-729", "name-104")
        assertEquals(ScheduledLockerReviewStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledLockerReview("code-729", "name-104")
        try {
            item.moveTo(ScheduledLockerReviewStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLockerReviewStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledLockerReview("code-729", "name-104")
        item.status = ScheduledLockerReviewStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledLockerReviewStatus.OPEN) }
    }
}
