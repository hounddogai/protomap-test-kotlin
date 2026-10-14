package com.acme.transport.emissions.carbonreport.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCarbonReportVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientCarbonReportVariance("code-404", "label-101")
        assertEquals(LenientCarbonReportVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLenientCarbonReportVariance("code-404", "label-101")
        try {
            item.moveTo(LenientCarbonReportVarianceStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCarbonReportVarianceStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientCarbonReportVariance("code-404", "label-101")
        item.status = LenientCarbonReportVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(LenientCarbonReportVarianceStatus.IDLE) }
    }
}
