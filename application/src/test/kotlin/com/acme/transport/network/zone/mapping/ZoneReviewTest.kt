package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newZoneReview("code-188", "name-645")
        assertEquals(ZoneReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newZoneReview("code-188", "name-645")
        try {
            item.moveTo(ZoneReviewStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneReviewStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newZoneReview("code-188", "name-645")
        item.status = ZoneReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ZoneReviewStatus.PROPOSED) }
    }
}
