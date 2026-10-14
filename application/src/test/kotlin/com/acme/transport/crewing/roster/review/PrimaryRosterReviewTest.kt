package com.acme.transport.crewing.roster.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRosterReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryRosterReview("code-335", "name-788")
        assertEquals(PrimaryRosterReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newPrimaryRosterReview("code-335", "name-788")
        try {
            item.moveTo(PrimaryRosterReviewStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRosterReviewStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryRosterReview("code-335", "name-788")
        item.status = PrimaryRosterReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryRosterReviewStatus.REQUESTED) }
    }
}
