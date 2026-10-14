package com.acme.transport.emissions.carbonreport.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newCarbonReportRecord("code-193", "label-983")
        assertEquals(CarbonReportRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCarbonReportRecord("code-193", "label-983")
        try {
            item.moveTo(CarbonReportRecordStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportRecordStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCarbonReportRecord("code-193", "label-983")
        item.status = CarbonReportRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CarbonReportRecordStatus.PROPOSED) }
    }
}
