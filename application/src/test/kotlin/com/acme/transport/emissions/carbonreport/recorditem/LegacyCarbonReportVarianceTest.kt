package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCarbonReportVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyCarbonReportVariance("code-69", "name-262")
        assertEquals(LegacyCarbonReportVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLegacyCarbonReportVariance("code-69", "name-262")
        try {
            item.moveTo(LegacyCarbonReportVarianceStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCarbonReportVarianceStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyCarbonReportVariance("code-69", "name-262")
        item.status = LegacyCarbonReportVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyCarbonReportVarianceStatus.PROPOSED) }
    }
}
