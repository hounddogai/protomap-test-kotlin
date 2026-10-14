package com.acme.transport.fleetops.defect.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newDefectEstimate("code-531", "label-819")
        assertEquals(DefectEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDefectEstimate("code-531", "label-819")
        try {
            item.moveTo(DefectEstimateStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectEstimateStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefectEstimate("code-531", "label-819")
        item.status = DefectEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(DefectEstimateStatus.IDLE) }
    }
}
