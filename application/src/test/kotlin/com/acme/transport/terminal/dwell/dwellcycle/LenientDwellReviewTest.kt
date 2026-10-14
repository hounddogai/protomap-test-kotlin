package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDwellReviewTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientDwellReview("code-824", "label-889")
        assertEquals(LenientDwellReviewStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientDwellReview("code-824", "label-889")
        try {
            item.moveTo(LenientDwellReviewStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDwellReviewStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientDwellReview("code-824", "label-889")
        item.status = LenientDwellReviewStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientDwellReviewStatus.PLANNED) }
    }
}
