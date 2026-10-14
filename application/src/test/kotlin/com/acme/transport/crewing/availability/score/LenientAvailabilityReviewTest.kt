package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAvailabilityReviewTest {
    @Test
    fun `starts in new`() {
        val item = newLenientAvailabilityReview("code-112", "label-778")
        assertEquals(LenientAvailabilityReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newLenientAvailabilityReview("code-112", "label-778")
        try {
            item.moveTo(LenientAvailabilityReviewStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAvailabilityReviewStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientAvailabilityReview("code-112", "label-778")
        item.status = LenientAvailabilityReviewStatus.FAILED
        assertFails("move") { item.moveTo(LenientAvailabilityReviewStatus.NEW) }
    }
}
