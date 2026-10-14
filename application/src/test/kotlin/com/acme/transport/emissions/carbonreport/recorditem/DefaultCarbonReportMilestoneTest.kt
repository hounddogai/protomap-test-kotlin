package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCarbonReportMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultCarbonReportMilestone("code-819", "title-808")
        assertEquals(DefaultCarbonReportMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDefaultCarbonReportMilestone("code-819", "title-808")
        try {
            item.moveTo(DefaultCarbonReportMilestoneStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCarbonReportMilestoneStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultCarbonReportMilestone("code-819", "title-808")
        item.status = DefaultCarbonReportMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultCarbonReportMilestoneStatus.PLANNED) }
    }
}
