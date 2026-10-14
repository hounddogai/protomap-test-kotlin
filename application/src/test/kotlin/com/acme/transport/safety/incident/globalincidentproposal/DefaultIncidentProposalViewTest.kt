package com.acme.transport.safety.incident.globalincidentproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultIncidentProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultIncidentProposal("code-938", "label-536")
        val view = toDefaultIncidentProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultIncidentProposalViews(mutableListOf<DefaultIncidentProposal>(item, item)).size, "views")
    }
}
