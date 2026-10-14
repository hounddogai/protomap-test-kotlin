package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFuelBurnProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingFuelBurnProposal("code-10", "name-751")
        assertEquals(RollingFuelBurnProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRollingFuelBurnProposal("code-10", "name-751")
        try {
            item.moveTo(RollingFuelBurnProposalStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFuelBurnProposalStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingFuelBurnProposal("code-10", "name-751")
        item.status = RollingFuelBurnProposalStatus.STOPPED
        assertFails("move") { item.moveTo(RollingFuelBurnProposalStatus.IDLE) }
    }
}
