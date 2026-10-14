package com.acme.transport.terminal.dwell.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellAssignmentTest {
    @Test
    fun `starts in new`() {
        val item = newDwellAssignment("code-828", "label-263")
        assertEquals(DwellAssignmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDwellAssignment("code-828", "label-263")
        try {
            item.moveTo(DwellAssignmentStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellAssignmentStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDwellAssignment("code-828", "label-263")
        item.status = DwellAssignmentStatus.FAILED
        assertFails("move") { item.moveTo(DwellAssignmentStatus.NEW) }
    }
}
