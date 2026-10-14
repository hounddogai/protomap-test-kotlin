package com.acme.transport.fleetops.inspection.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionProposalTest {
    @Test
    fun `starts in open`() {
        val item = newInspectionProposal("code-824", "title-459")
        assertEquals(InspectionProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newInspectionProposal("code-824", "title-459")
        try {
            item.moveTo(InspectionProposalStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionProposalStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newInspectionProposal("code-824", "title-459")
        item.status = InspectionProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(InspectionProposalStatus.OPEN) }
    }
}
