package com.acme.transport.crewing.roster.rostersummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newRosterReview("code-644", "name-48")
        assertEquals(RosterReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRosterReview("code-644", "name-48")
        try {
            item.moveTo(RosterReviewStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterReviewStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRosterReview("code-644", "name-48")
        item.status = RosterReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RosterReviewStatus.PROPOSED) }
    }
}
