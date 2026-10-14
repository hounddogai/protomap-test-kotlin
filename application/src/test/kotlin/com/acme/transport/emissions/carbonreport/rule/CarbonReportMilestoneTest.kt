package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newCarbonReportMilestone("code-994", "label-234")
        assertEquals(CarbonReportMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCarbonReportMilestone("code-994", "label-234")
        try {
            item.moveTo(CarbonReportMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCarbonReportMilestone("code-994", "label-234")
        item.status = CarbonReportMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(CarbonReportMilestoneStatus.CREATED) }
    }
}
