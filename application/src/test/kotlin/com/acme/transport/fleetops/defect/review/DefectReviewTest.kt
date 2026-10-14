package com.acme.transport.fleetops.defect.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefectReview("code-706", "name-60")
        assertEquals(DefectReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDefectReview("code-706", "name-60")
        try {
            item.moveTo(DefectReviewStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectReviewStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefectReview("code-706", "name-60")
        item.status = DefectReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefectReviewStatus.PROPOSED) }
    }
}
