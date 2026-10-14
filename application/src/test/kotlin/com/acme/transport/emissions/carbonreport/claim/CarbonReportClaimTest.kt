package com.acme.transport.emissions.carbonreport.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportClaimTest {
    @Test
    fun `starts in open`() {
        val item = newCarbonReportClaim("code-958", "label-101")
        assertEquals(CarbonReportClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCarbonReportClaim("code-958", "label-101")
        try {
            item.moveTo(CarbonReportClaimStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportClaimStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCarbonReportClaim("code-958", "label-101")
        item.status = CarbonReportClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(CarbonReportClaimStatus.OPEN) }
    }
}
