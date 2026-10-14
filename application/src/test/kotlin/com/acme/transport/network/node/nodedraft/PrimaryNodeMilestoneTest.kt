package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryNodeMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimaryNodeMilestone("code-282", "name-352")
        assertEquals(PrimaryNodeMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newPrimaryNodeMilestone("code-282", "name-352")
        try {
            item.moveTo(PrimaryNodeMilestoneStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryNodeMilestoneStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimaryNodeMilestone("code-282", "name-352")
        item.status = PrimaryNodeMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(PrimaryNodeMilestoneStatus.DRAFT) }
    }
}
