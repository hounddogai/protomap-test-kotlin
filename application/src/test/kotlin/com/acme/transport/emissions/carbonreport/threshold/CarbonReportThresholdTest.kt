package com.acme.transport.emissions.carbonreport.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newCarbonReportThreshold("code-879", "title-666")
        assertEquals(CarbonReportThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newCarbonReportThreshold("code-879", "title-666")
        try {
            item.moveTo(CarbonReportThresholdStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportThresholdStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCarbonReportThreshold("code-879", "title-666")
        item.status = CarbonReportThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(CarbonReportThresholdStatus.IDLE) }
    }
}
