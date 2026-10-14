package com.acme.transport.network.node.nodegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyNodeAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyNodeAssignment("code-13", "name-300")
        assertEquals(LegacyNodeAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLegacyNodeAssignment("code-13", "name-300")
        try {
            item.moveTo(LegacyNodeAssignmentStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyNodeAssignmentStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyNodeAssignment("code-13", "name-300")
        item.status = LegacyNodeAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyNodeAssignmentStatus.IDLE) }
    }
}
