package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newLanePlanAssignment("code-105", "name-173")
        assertEquals(LanePlanAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLanePlanAssignment("code-105", "name-173")
        try {
            item.moveTo(LanePlanAssignmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanAssignmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLanePlanAssignment("code-105", "name-173")
        item.status = LanePlanAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(LanePlanAssignmentStatus.IDLE) }
    }
}
