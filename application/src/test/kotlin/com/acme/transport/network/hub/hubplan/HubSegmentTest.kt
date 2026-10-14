package com.acme.transport.network.hub.hubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newHubSegment("code-700", "title-233")
        assertEquals(HubSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newHubSegment("code-700", "title-233")
        try {
            item.moveTo(HubSegmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubSegmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newHubSegment("code-700", "title-233")
        item.status = HubSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(HubSegmentStatus.PLANNED) }
    }
}
