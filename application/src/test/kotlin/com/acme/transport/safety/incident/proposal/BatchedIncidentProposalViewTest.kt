package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedIncidentProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedIncidentProposal("code-60", "label-458")
        val view = toBatchedIncidentProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedIncidentProposalViews(mutableListOf<BatchedIncidentProposal>(item, item)).size, "views")
    }
}
