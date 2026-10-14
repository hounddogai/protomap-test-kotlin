package com.acme.transport.emissions.carbonreport.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCarbonReportRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalCarbonReportRequest("code-926", "title-468")
        assertEquals(RegionalCarbonReportRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRegionalCarbonReportRequest("code-926", "title-468")
        try {
            item.moveTo(RegionalCarbonReportRequestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCarbonReportRequestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalCarbonReportRequest("code-926", "title-468")
        item.status = RegionalCarbonReportRequestStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalCarbonReportRequestStatus.IDLE) }
    }
}
