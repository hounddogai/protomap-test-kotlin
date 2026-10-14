package com.acme.transport.emissions.carbonreport.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newCarbonReportQuota("code-438", "title-768")
        assertEquals(CarbonReportQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCarbonReportQuota("code-438", "title-768")
        try {
            item.moveTo(CarbonReportQuotaStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportQuotaStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCarbonReportQuota("code-438", "title-768")
        item.status = CarbonReportQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CarbonReportQuotaStatus.PROPOSED) }
    }
}
