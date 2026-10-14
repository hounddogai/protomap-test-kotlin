package com.acme.transport.linehaul.relay.weightedrelayrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRelayRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingRelayRollup("code-175", "title-503")
        assertEquals(PendingRelayRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPendingRelayRollup("code-175", "title-503")
        try {
            item.moveTo(PendingRelayRollupStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRelayRollupStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingRelayRollup("code-175", "title-503")
        item.status = PendingRelayRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingRelayRollupStatus.PLANNED) }
    }
}
