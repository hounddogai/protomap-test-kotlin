package com.acme.transport.emissions.carbonreport.reportthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCarbonReportWindowTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledCarbonReportWindow("code-360", "title-997")
        assertEquals(ScheduledCarbonReportWindowStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newScheduledCarbonReportWindow("code-360", "title-997")
        try {
            item.moveTo(ScheduledCarbonReportWindowStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCarbonReportWindowStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledCarbonReportWindow("code-360", "title-997")
        item.status = ScheduledCarbonReportWindowStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledCarbonReportWindowStatus.IDLE) }
    }
}
