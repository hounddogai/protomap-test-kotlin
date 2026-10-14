package com.acme.transport.safety.violation.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledViolationProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledViolationProposal("code-765", "title-46")
        val view = toScheduledViolationProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledViolationProposalViews(mutableListOf<ScheduledViolationProposal>(item, item)).size, "views")
    }
}
