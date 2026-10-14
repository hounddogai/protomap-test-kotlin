package com.acme.transport.emissions.carbonreport.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCarbonReportAssignmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalCarbonReportAssignment("code-265", "name-613")
        assertEquals(RegionalCarbonReportAssignmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newRegionalCarbonReportAssignment("code-265", "name-613")
        try {
            item.moveTo(RegionalCarbonReportAssignmentStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCarbonReportAssignmentStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalCarbonReportAssignment("code-265", "name-613")
        item.status = RegionalCarbonReportAssignmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalCarbonReportAssignmentStatus.PROPOSED) }
    }
}
