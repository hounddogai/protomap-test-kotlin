package com.acme.transport.emissions.carbonreport.reportbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportTierTest {
    @Test
    fun `starts in new`() {
        val item = newCarbonReportTier("code-661", "label-395")
        assertEquals(CarbonReportTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCarbonReportTier("code-661", "label-395")
        try {
            item.moveTo(CarbonReportTierStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportTierStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCarbonReportTier("code-661", "label-395")
        item.status = CarbonReportTierStatus.FAILED
        assertFails("move") { item.moveTo(CarbonReportTierStatus.NEW) }
    }
}
