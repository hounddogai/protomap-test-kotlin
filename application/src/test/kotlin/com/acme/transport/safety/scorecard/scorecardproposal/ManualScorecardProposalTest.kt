package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualScorecardProposalTest {
    @Test
    fun `starts in new`() {
        val item = newManualScorecardProposal("code-584", "label-795")
        assertEquals(ManualScorecardProposalStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newManualScorecardProposal("code-584", "label-795")
        try {
            item.moveTo(ManualScorecardProposalStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualScorecardProposalStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualScorecardProposal("code-584", "label-795")
        item.status = ManualScorecardProposalStatus.FAILED
        assertFails("move") { item.moveTo(ManualScorecardProposalStatus.NEW) }
    }
}
