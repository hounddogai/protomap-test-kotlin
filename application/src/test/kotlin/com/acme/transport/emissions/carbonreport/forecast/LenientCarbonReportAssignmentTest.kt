package com.acme.transport.emissions.carbonreport.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCarbonReportAssignmentTest {
    @Test
    fun `starts in open`() {
        val item = newLenientCarbonReportAssignment("code-425", "title-636")
        assertEquals(LenientCarbonReportAssignmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newLenientCarbonReportAssignment("code-425", "title-636")
        try {
            item.moveTo(LenientCarbonReportAssignmentStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCarbonReportAssignmentStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientCarbonReportAssignment("code-425", "title-636")
        item.status = LenientCarbonReportAssignmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientCarbonReportAssignmentStatus.OPEN) }
    }
}
