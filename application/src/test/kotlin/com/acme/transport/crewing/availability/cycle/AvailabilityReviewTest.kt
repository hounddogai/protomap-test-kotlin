package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newAvailabilityReview("code-332", "name-317")
        assertEquals(AvailabilityReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newAvailabilityReview("code-332", "name-317")
        try {
            item.moveTo(AvailabilityReviewStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityReviewStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAvailabilityReview("code-332", "name-317")
        item.status = AvailabilityReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AvailabilityReviewStatus.PROPOSED) }
    }
}
