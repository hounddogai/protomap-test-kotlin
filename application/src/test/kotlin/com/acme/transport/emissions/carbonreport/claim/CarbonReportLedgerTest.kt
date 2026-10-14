package com.acme.transport.emissions.carbonreport.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newCarbonReportLedger("code-535", "label-485")
        assertEquals(CarbonReportLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCarbonReportLedger("code-535", "label-485")
        try {
            item.moveTo(CarbonReportLedgerStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportLedgerStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCarbonReportLedger("code-535", "label-485")
        item.status = CarbonReportLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CarbonReportLedgerStatus.PROPOSED) }
    }
}
