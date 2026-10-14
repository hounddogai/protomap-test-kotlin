package com.acme.transport.terminal.dwell.dwellreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDwellReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyDwellReview("code-823", "label-647")
        assertEquals(LegacyDwellReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLegacyDwellReview("code-823", "label-647")
        try {
            item.moveTo(LegacyDwellReviewStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDwellReviewStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyDwellReview("code-823", "label-647")
        item.status = LegacyDwellReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyDwellReviewStatus.REQUESTED) }
    }
}
