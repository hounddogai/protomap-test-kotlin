package com.acme.transport.fleetops.inspection.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictInspectionProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictInspectionProposal("code-270", "name-906")
        assertEquals(StrictInspectionProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newStrictInspectionProposal("code-270", "name-906")
        try {
            item.moveTo(StrictInspectionProposalStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictInspectionProposalStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictInspectionProposal("code-270", "name-906")
        item.status = StrictInspectionProposalStatus.STOPPED
        assertFails("move") { item.moveTo(StrictInspectionProposalStatus.IDLE) }
    }
}
