package com.acme.transport.terminal.dwell.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newDwellMilestone("code-455", "label-905")
        assertEquals(DwellMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDwellMilestone("code-455", "label-905")
        try {
            item.moveTo(DwellMilestoneStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellMilestoneStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDwellMilestone("code-455", "label-905")
        item.status = DwellMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(DwellMilestoneStatus.NEW) }
    }
}
