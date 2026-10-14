package com.acme.transport.emissions.fuelburn.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newFuelBurnProposal("code-761", "name-265")
        assertEquals(FuelBurnProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newFuelBurnProposal("code-761", "name-265")
        try {
            item.moveTo(FuelBurnProposalStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnProposalStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFuelBurnProposal("code-761", "name-265")
        item.status = FuelBurnProposalStatus.STOPPED
        assertFails("move") { item.moveTo(FuelBurnProposalStatus.IDLE) }
    }
}
