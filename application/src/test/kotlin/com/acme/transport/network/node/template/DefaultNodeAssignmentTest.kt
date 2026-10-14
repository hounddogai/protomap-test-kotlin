package com.acme.transport.network.node.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultNodeAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultNodeAssignment("code-499", "title-712")
        assertEquals(DefaultNodeAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDefaultNodeAssignment("code-499", "title-712")
        try {
            item.moveTo(DefaultNodeAssignmentStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultNodeAssignmentStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultNodeAssignment("code-499", "title-712")
        item.status = DefaultNodeAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultNodeAssignmentStatus.IDLE) }
    }
}
