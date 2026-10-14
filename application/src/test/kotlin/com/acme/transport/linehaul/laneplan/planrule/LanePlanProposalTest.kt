package com.acme.transport.linehaul.laneplan.planrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newLanePlanProposal("code-717", "title-867")
        assertEquals(LanePlanProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newLanePlanProposal("code-717", "title-867")
        try {
            item.moveTo(LanePlanProposalStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanProposalStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLanePlanProposal("code-717", "title-867")
        item.status = LanePlanProposalStatus.STOPPED
        assertFails("move") { item.moveTo(LanePlanProposalStatus.IDLE) }
    }
}
