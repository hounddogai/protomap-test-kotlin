package com.acme.transport.emissions.carbonreport.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCarbonReportTierTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedCarbonReportTier("code-357", "title-186")
        assertEquals(WeightedCarbonReportTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newWeightedCarbonReportTier("code-357", "title-186")
        try {
            item.moveTo(WeightedCarbonReportTierStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCarbonReportTierStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedCarbonReportTier("code-357", "title-186")
        item.status = WeightedCarbonReportTierStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedCarbonReportTierStatus.REQUESTED) }
    }
}
