package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newCarbonReportProposal("code-237", "title-389")
        assertEquals(CarbonReportProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newCarbonReportProposal("code-237", "title-389")
        try {
            item.moveTo(CarbonReportProposalStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportProposalStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCarbonReportProposal("code-237", "title-389")
        item.status = CarbonReportProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(CarbonReportProposalStatus.REQUESTED) }
    }
}
