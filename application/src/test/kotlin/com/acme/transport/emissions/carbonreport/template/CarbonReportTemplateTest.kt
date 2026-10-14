package com.acme.transport.emissions.carbonreport.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newCarbonReportTemplate("code-931", "title-422")
        assertEquals(CarbonReportTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newCarbonReportTemplate("code-931", "title-422")
        try {
            item.moveTo(CarbonReportTemplateStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportTemplateStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCarbonReportTemplate("code-931", "title-422")
        item.status = CarbonReportTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(CarbonReportTemplateStatus.PLANNED) }
    }
}
