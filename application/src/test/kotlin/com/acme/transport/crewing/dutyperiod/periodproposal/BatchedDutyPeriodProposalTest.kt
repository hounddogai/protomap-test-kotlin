package com.acme.transport.crewing.dutyperiod.periodproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDutyPeriodProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedDutyPeriodProposal("code-733", "label-212")
        assertEquals(BatchedDutyPeriodProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedDutyPeriodProposal("code-733", "label-212")
        try {
            item.moveTo(BatchedDutyPeriodProposalStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDutyPeriodProposalStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedDutyPeriodProposal("code-733", "label-212")
        item.status = BatchedDutyPeriodProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedDutyPeriodProposalStatus.REQUESTED) }
    }
}
