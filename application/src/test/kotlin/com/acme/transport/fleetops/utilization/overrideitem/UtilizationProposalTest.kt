package com.acme.transport.fleetops.utilization.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationProposalTest {
    @Test
    fun `starts in proposed`() {
        val item = newUtilizationProposal("code-897", "name-843")
        assertEquals(UtilizationProposalStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newUtilizationProposal("code-897", "name-843")
        try {
            item.moveTo(UtilizationProposalStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationProposalStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newUtilizationProposal("code-897", "name-843")
        item.status = UtilizationProposalStatus.WITHDRAWN
        assertFails("move") { item.moveTo(UtilizationProposalStatus.PROPOSED) }
    }
}
