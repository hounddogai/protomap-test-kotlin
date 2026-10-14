package com.acme.transport.emissions.carbonreport.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newCarbonReportAssignment("code-220", "name-189")
        assertEquals(CarbonReportAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCarbonReportAssignment("code-220", "name-189")
        try {
            item.moveTo(CarbonReportAssignmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportAssignmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCarbonReportAssignment("code-220", "name-189")
        item.status = CarbonReportAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(CarbonReportAssignmentStatus.REQUESTED) }
    }
}
