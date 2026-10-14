package com.acme.transport.emissions.carbonreport.regionalreportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCarbonReportProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictCarbonReportProposal("code-770", "name-384")
        assertEquals(StrictCarbonReportProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newStrictCarbonReportProposal("code-770", "name-384")
        try {
            item.moveTo(StrictCarbonReportProposalStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCarbonReportProposalStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictCarbonReportProposal("code-770", "name-384")
        item.status = StrictCarbonReportProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictCarbonReportProposalStatus.REQUESTED) }
    }
}
