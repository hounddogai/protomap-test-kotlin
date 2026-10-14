package com.acme.transport.lastmile.parcel.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualParcelProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newManualParcelProposal("code-884", "label-413")
        assertEquals(ManualParcelProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualParcelProposal("code-884", "label-413")
        try {
            item.moveTo(ManualParcelProposalStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualParcelProposalStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualParcelProposal("code-884", "label-413")
        item.status = ManualParcelProposalStatus.STOPPED
        assertFails("move") { item.moveTo(ManualParcelProposalStatus.IDLE) }
    }
}
