package com.acme.transport.linehaul.relay.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRelayAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newTieredRelayAssignment("code-466", "name-774")
        assertEquals(TieredRelayAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredRelayAssignment("code-466", "name-774")
        try {
            item.moveTo(TieredRelayAssignmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRelayAssignmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredRelayAssignment("code-466", "name-774")
        item.status = TieredRelayAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(TieredRelayAssignmentStatus.CREATED) }
    }
}
