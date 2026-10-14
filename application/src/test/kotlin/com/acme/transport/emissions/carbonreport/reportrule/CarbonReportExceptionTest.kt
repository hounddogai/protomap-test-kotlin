package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newCarbonReportException("code-387", "label-252")
        assertEquals(CarbonReportExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCarbonReportException("code-387", "label-252")
        try {
            item.moveTo(CarbonReportExceptionStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportExceptionStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCarbonReportException("code-387", "label-252")
        item.status = CarbonReportExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(CarbonReportExceptionStatus.IDLE) }
    }
}
