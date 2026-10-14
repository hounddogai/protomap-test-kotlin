package com.acme.transport.network.transittime.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTransitTimeProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredTransitTimeProposal("code-405", "label-687")
        assertEquals(TieredTransitTimeProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTieredTransitTimeProposal("code-405", "label-687")
        try {
            item.moveTo(TieredTransitTimeProposalStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTransitTimeProposalStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredTransitTimeProposal("code-405", "label-687")
        item.status = TieredTransitTimeProposalStatus.STOPPED
        assertFails("move") { item.moveTo(TieredTransitTimeProposalStatus.IDLE) }
    }
}
