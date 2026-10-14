package com.acme.transport.safety.violation.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictViolationProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictViolationProposal("code-75", "name-104")
        val view = toStrictViolationProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictViolationProposalViews(mutableListOf<StrictViolationProposal>(item, item)).size, "views")
    }
}
