package com.acme.transport.linehaul.tender.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newTenderProposal("code-788", "label-613")
        assertEquals(TenderProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newTenderProposal("code-788", "label-613")
        try {
            item.moveTo(TenderProposalStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderProposalStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTenderProposal("code-788", "label-613")
        item.status = TenderProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(TenderProposalStatus.PLANNED) }
    }
}
