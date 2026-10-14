package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentProposal("code-553", "label-638")
        val view = toIncidentProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentProposalViews(mutableListOf<IncidentProposal>(item, item)).size, "views")
    }
}
