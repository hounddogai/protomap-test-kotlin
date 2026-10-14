package com.acme.transport.fleetops.inspection.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionReviewTest {
    @Test
    fun `starts in planned`() {
        val item = newInspectionReview("code-114", "title-635")
        assertEquals(InspectionReviewStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newInspectionReview("code-114", "title-635")
        try {
            item.moveTo(InspectionReviewStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionReviewStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newInspectionReview("code-114", "title-635")
        item.status = InspectionReviewStatus.CANCELLED
        assertFails("move") { item.moveTo(InspectionReviewStatus.PLANNED) }
    }
}
