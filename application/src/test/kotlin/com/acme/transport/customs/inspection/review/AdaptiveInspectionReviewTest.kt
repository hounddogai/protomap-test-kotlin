package com.acme.transport.customs.inspection.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveInspectionReview("code-15", "name-848")
        assertEquals(AdaptiveInspectionReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAdaptiveInspectionReview("code-15", "name-848")
        try {
            item.moveTo(AdaptiveInspectionReviewStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionReviewStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveInspectionReview("code-15", "name-848")
        item.status = AdaptiveInspectionReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveInspectionReviewStatus.REQUESTED) }
    }
}
