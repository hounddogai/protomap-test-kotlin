package com.acme.transport.emissions.carbonreport.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newCarbonReportEstimate("code-595", "title-347")
        assertEquals(CarbonReportEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCarbonReportEstimate("code-595", "title-347")
        try {
            item.moveTo(CarbonReportEstimateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportEstimateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCarbonReportEstimate("code-595", "title-347")
        item.status = CarbonReportEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(CarbonReportEstimateStatus.CREATED) }
    }
}
