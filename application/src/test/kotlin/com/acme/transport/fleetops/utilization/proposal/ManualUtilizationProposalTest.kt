package com.acme.transport.fleetops.utilization.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualUtilizationProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newManualUtilizationProposal("code-342", "name-247")
        assertEquals(ManualUtilizationProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newManualUtilizationProposal("code-342", "name-247")
        try {
            item.moveTo(ManualUtilizationProposalStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualUtilizationProposalStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualUtilizationProposal("code-342", "name-247")
        item.status = ManualUtilizationProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualUtilizationProposalStatus.REQUESTED) }
    }
}
