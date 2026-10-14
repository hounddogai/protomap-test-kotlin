package com.acme.transport.crewing.dutyperiod.periodproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDutyPeriodProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDutyPeriodProposal("code-733", "label-212")
        val view = toBatchedDutyPeriodProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDutyPeriodProposalViews(mutableListOf<BatchedDutyPeriodProposal>(item, item)).size, "views")
    }
}
