package com.acme.transport.customs.inspection.inspectionplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientInspectionPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientInspectionPlan("code-866", "title-387")
        assertEquals(LenientInspectionPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLenientInspectionPlan("code-866", "title-387")
        try {
            item.moveTo(LenientInspectionPlanStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientInspectionPlanStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientInspectionPlan("code-866", "title-387")
        item.status = LenientInspectionPlanStatus.STOPPED
        assertFails("move") { item.moveTo(LenientInspectionPlanStatus.IDLE) }
    }
}
