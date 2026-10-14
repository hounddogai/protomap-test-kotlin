package com.acme.transport.freightpay.dispute.disputemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDisputeSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalDisputeSegment("code-934", "title-965")
        assertEquals(SeasonalDisputeSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newSeasonalDisputeSegment("code-934", "title-965")
        try {
            item.moveTo(SeasonalDisputeSegmentStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDisputeSegmentStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalDisputeSegment("code-934", "title-965")
        item.status = SeasonalDisputeSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalDisputeSegmentStatus.PLANNED) }
    }
}
