package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityReviewTest {
    @Test
    fun `starts in open`() {
        val item = newCapacityReview("code-179", "label-504")
        assertEquals(CapacityReviewStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCapacityReview("code-179", "label-504")
        try {
            item.moveTo(CapacityReviewStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityReviewStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCapacityReview("code-179", "label-504")
        item.status = CapacityReviewStatus.ARCHIVED
        assertFails("move") { item.moveTo(CapacityReviewStatus.OPEN) }
    }
}
