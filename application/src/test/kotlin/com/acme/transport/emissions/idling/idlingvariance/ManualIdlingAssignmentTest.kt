package com.acme.transport.emissions.idling.idlingvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualIdlingAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newManualIdlingAssignment("code-449", "title-798")
        assertEquals(ManualIdlingAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newManualIdlingAssignment("code-449", "title-798")
        try {
            item.moveTo(ManualIdlingAssignmentStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualIdlingAssignmentStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualIdlingAssignment("code-449", "title-798")
        item.status = ManualIdlingAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(ManualIdlingAssignmentStatus.IDLE) }
    }
}
