package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newRelayMilestone("code-212", "name-522")
        assertEquals(RelayMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRelayMilestone("code-212", "name-522")
        try {
            item.moveTo(RelayMilestoneStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayMilestoneStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRelayMilestone("code-212", "name-522")
        item.status = RelayMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(RelayMilestoneStatus.OPEN) }
    }
}
