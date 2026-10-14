package com.acme.transport.emissions.carbonreport.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCarbonReportAssignmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingCarbonReportAssignment("code-347", "name-665")
        assertEquals(RollingCarbonReportAssignmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingCarbonReportAssignment("code-347", "name-665")
        try {
            item.moveTo(RollingCarbonReportAssignmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCarbonReportAssignmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingCarbonReportAssignment("code-347", "name-665")
        item.status = RollingCarbonReportAssignmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingCarbonReportAssignmentStatus.PROPOSED) }
    }
}
