package com.acme.transport.terminal.traileryard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTrailerYardAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newManualTrailerYardAssignment("code-982", "title-436")
        assertEquals(ManualTrailerYardAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualTrailerYardAssignment("code-982", "title-436")
        try {
            item.moveTo(ManualTrailerYardAssignmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTrailerYardAssignmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualTrailerYardAssignment("code-982", "title-436")
        item.status = ManualTrailerYardAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(ManualTrailerYardAssignmentStatus.IDLE) }
    }
}
