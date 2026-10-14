package com.acme.transport.emissions.carbonreport.reportscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCarbonReportAssignmentTest {
    @Test
    fun `starts in open`() {
        val item = newCappedCarbonReportAssignment("code-206", "label-943")
        assertEquals(CappedCarbonReportAssignmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCappedCarbonReportAssignment("code-206", "label-943")
        try {
            item.moveTo(CappedCarbonReportAssignmentStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCarbonReportAssignmentStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedCarbonReportAssignment("code-206", "label-943")
        item.status = CappedCarbonReportAssignmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedCarbonReportAssignmentStatus.OPEN) }
    }
}
