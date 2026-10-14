package com.acme.transport.fleetops.defect.defectreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDefectReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingDefectReview("code-671", "name-462")
        assertEquals(PendingDefectReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingDefectReview("code-671", "name-462")
        try {
            item.moveTo(PendingDefectReviewStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDefectReviewStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingDefectReview("code-671", "name-462")
        item.status = PendingDefectReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingDefectReviewStatus.REQUESTED) }
    }
}
