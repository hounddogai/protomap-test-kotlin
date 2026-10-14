package com.acme.transport.customs.declaration.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDeclarationReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalDeclarationReview("code-962", "name-241")
        assertEquals(SeasonalDeclarationReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newSeasonalDeclarationReview("code-962", "name-241")
        try {
            item.moveTo(SeasonalDeclarationReviewStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDeclarationReviewStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalDeclarationReview("code-962", "name-241")
        item.status = SeasonalDeclarationReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalDeclarationReviewStatus.PROPOSED) }
    }
}
