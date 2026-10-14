package com.acme.transport.linehaul.relay.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelaySegmentTest {
    @Test
    fun `starts in created`() {
        val item = newRelaySegment("code-239", "label-487")
        assertEquals(RelaySegmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRelaySegment("code-239", "label-487")
        try {
            item.moveTo(RelaySegmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelaySegmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRelaySegment("code-239", "label-487")
        item.status = RelaySegmentStatus.RETIRED
        assertFails("move") { item.moveTo(RelaySegmentStatus.CREATED) }
    }
}
