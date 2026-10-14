package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedAttemptProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedAttemptProposal("code-976", "label-357")
        val view = toBatchedAttemptProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedAttemptProposalViews(mutableListOf<BatchedAttemptProposal>(item, item)).size, "views")
    }
}
