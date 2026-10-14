package com.acme.transport.fleetops.defect.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newDefectMilestone("code-153", "label-501")
        assertEquals(DefectMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDefectMilestone("code-153", "label-501")
        try {
            item.moveTo(DefectMilestoneStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectMilestoneStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefectMilestone("code-153", "label-501")
        item.status = DefectMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(DefectMilestoneStatus.REQUESTED) }
    }
}
