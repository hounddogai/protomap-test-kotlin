package com.acme.transport.emissions.carbonreport.reportestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportMetricTest {
    @Test
    fun `starts in created`() {
        val item = newCarbonReportMetric("code-1", "label-244")
        assertEquals(CarbonReportMetricStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCarbonReportMetric("code-1", "label-244")
        try {
            item.moveTo(CarbonReportMetricStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportMetricStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCarbonReportMetric("code-1", "label-244")
        item.status = CarbonReportMetricStatus.RETIRED
        assertFails("move") { item.moveTo(CarbonReportMetricStatus.CREATED) }
    }
}
