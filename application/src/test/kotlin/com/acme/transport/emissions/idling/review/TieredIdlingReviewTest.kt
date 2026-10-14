package com.acme.transport.emissions.idling.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredIdlingReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredIdlingReview("code-380", "name-422")
        assertEquals(TieredIdlingReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTieredIdlingReview("code-380", "name-422")
        try {
            item.moveTo(TieredIdlingReviewStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredIdlingReviewStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredIdlingReview("code-380", "name-422")
        item.status = TieredIdlingReviewStatus.STOPPED
        assertFails("move") { item.moveTo(TieredIdlingReviewStatus.IDLE) }
    }
}
