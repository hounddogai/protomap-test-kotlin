package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCarbonReportVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredCarbonReportVariance("code-662", "name-184")
        assertEquals(TieredCarbonReportVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newTieredCarbonReportVariance("code-662", "name-184")
        try {
            item.moveTo(TieredCarbonReportVarianceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCarbonReportVarianceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredCarbonReportVariance("code-662", "name-184")
        item.status = TieredCarbonReportVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredCarbonReportVarianceStatus.PLANNED) }
    }
}
