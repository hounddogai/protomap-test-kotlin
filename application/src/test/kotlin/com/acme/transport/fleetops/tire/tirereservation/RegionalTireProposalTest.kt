package com.acme.transport.fleetops.tire.tirereservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTireProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalTireProposal("code-189", "name-519")
        assertEquals(RegionalTireProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalTireProposal("code-189", "name-519")
        try {
            item.moveTo(RegionalTireProposalStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTireProposalStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalTireProposal("code-189", "name-519")
        item.status = RegionalTireProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalTireProposalStatus.REQUESTED) }
    }
}
