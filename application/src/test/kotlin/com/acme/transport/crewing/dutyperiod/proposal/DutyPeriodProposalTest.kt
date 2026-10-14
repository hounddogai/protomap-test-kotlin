package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodProposalTest {
    @Test
    fun `starts in proposed`() {
        val item = newDutyPeriodProposal("code-566", "title-46")
        assertEquals(DutyPeriodProposalStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newDutyPeriodProposal("code-566", "title-46")
        try {
            item.moveTo(DutyPeriodProposalStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodProposalStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDutyPeriodProposal("code-566", "title-46")
        item.status = DutyPeriodProposalStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DutyPeriodProposalStatus.PROPOSED) }
    }
}
