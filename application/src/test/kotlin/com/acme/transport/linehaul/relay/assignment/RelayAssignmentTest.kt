package com.acme.transport.linehaul.relay.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayAssignmentTest {
    @Test
    fun `starts in new`() {
        val item = newRelayAssignment("code-8", "title-657")
        assertEquals(RelayAssignmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newRelayAssignment("code-8", "title-657")
        try {
            item.moveTo(RelayAssignmentStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayAssignmentStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRelayAssignment("code-8", "title-657")
        item.status = RelayAssignmentStatus.FAILED
        assertFails("move") { item.moveTo(RelayAssignmentStatus.NEW) }
    }
}
