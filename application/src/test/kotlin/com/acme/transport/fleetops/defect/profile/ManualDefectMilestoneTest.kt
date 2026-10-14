package com.acme.transport.fleetops.defect.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDefectMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newManualDefectMilestone("code-532", "name-728")
        assertEquals(ManualDefectMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newManualDefectMilestone("code-532", "name-728")
        try {
            item.moveTo(ManualDefectMilestoneStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDefectMilestoneStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualDefectMilestone("code-532", "name-728")
        item.status = ManualDefectMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(ManualDefectMilestoneStatus.IDLE) }
    }
}
