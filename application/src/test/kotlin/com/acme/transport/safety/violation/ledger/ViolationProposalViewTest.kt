package com.acme.transport.safety.violation.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationProposal("code-929", "title-143")
        val view = toViolationProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationProposalViews(mutableListOf<ViolationProposal>(item, item)).size, "views")
    }
}
