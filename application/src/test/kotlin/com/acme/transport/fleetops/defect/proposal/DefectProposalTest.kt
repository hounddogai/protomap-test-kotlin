package com.acme.transport.fleetops.defect.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newDefectProposal("code-175", "name-168")
        assertEquals(DefectProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newDefectProposal("code-175", "name-168")
        try {
            item.moveTo(DefectProposalStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectProposalStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefectProposal("code-175", "name-168")
        item.status = DefectProposalStatus.STOPPED
        assertFails("move") { item.moveTo(DefectProposalStatus.IDLE) }
    }
}
