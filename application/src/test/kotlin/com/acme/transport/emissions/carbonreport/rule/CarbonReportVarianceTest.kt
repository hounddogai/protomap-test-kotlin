package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newCarbonReportVariance("code-941", "label-352")
        assertEquals(CarbonReportVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCarbonReportVariance("code-941", "label-352")
        try {
            item.moveTo(CarbonReportVarianceStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportVarianceStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCarbonReportVariance("code-941", "label-352")
        item.status = CarbonReportVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(CarbonReportVarianceStatus.CREATED) }
    }
}
