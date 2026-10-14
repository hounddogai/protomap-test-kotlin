package com.acme.transport.emissions.fuelburn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledFuelBurnProposalTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledFuelBurnProposal("code-980", "name-615")
        assertEquals(ScheduledFuelBurnProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newScheduledFuelBurnProposal("code-980", "name-615")
        try {
            item.moveTo(ScheduledFuelBurnProposalStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledFuelBurnProposalStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledFuelBurnProposal("code-980", "name-615")
        item.status = ScheduledFuelBurnProposalStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledFuelBurnProposalStatus.CREATED) }
    }
}
