package com.acme.transport.linehaul.relay.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRelaySegmentTest {
    @Test
    fun `starts in new`() {
        val item = newPendingRelaySegment("code-398", "name-135")
        assertEquals(PendingRelaySegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newPendingRelaySegment("code-398", "name-135")
        try {
            item.moveTo(PendingRelaySegmentStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRelaySegmentStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingRelaySegment("code-398", "name-135")
        item.status = PendingRelaySegmentStatus.FAILED
        assertFails("move") { item.moveTo(PendingRelaySegmentStatus.NEW) }
    }
}
