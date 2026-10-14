package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTenderProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingTenderProposal("code-80", "name-96")
        assertEquals(PendingTenderProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newPendingTenderProposal("code-80", "name-96")
        try {
            item.moveTo(PendingTenderProposalStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTenderProposalStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingTenderProposal("code-80", "name-96")
        item.status = PendingTenderProposalStatus.STOPPED
        assertFails("move") { item.moveTo(PendingTenderProposalStatus.IDLE) }
    }
}
