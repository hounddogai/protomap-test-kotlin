package com.acme.transport.emissions.carbonreport.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCarbonReportClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedCarbonReportClaim("code-689", "name-744")
        assertEquals(WeightedCarbonReportClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newWeightedCarbonReportClaim("code-689", "name-744")
        try {
            item.moveTo(WeightedCarbonReportClaimStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCarbonReportClaimStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedCarbonReportClaim("code-689", "name-744")
        item.status = WeightedCarbonReportClaimStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedCarbonReportClaimStatus.DRAFT) }
    }
}
