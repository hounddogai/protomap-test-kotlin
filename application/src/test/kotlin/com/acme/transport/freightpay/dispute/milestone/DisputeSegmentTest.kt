package com.acme.transport.freightpay.dispute.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newDisputeSegment("code-542", "title-518")
        assertEquals(DisputeSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDisputeSegment("code-542", "title-518")
        try {
            item.moveTo(DisputeSegmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeSegmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDisputeSegment("code-542", "title-518")
        item.status = DisputeSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(DisputeSegmentStatus.IDLE) }
    }
}
