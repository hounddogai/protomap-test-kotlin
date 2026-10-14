package com.acme.transport.linehaul.relay.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRelayAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newManualRelayAssignment("code-9", "title-658")
        assertEquals(ManualRelayAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualRelayAssignment("code-9", "title-658")
        try {
            item.moveTo(ManualRelayAssignmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRelayAssignmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualRelayAssignment("code-9", "title-658")
        item.status = ManualRelayAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(ManualRelayAssignmentStatus.CREATED) }
    }
}
