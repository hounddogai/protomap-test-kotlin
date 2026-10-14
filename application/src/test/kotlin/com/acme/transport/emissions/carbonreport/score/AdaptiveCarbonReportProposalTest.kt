package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCarbonReportProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveCarbonReportProposal("code-100", "title-567")
        assertEquals(AdaptiveCarbonReportProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newAdaptiveCarbonReportProposal("code-100", "title-567")
        try {
            item.moveTo(AdaptiveCarbonReportProposalStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCarbonReportProposalStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveCarbonReportProposal("code-100", "title-567")
        item.status = AdaptiveCarbonReportProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveCarbonReportProposalStatus.PLANNED) }
    }
}
