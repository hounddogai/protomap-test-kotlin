package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientIncidentProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientIncidentProposal("code-969", "name-749")
        val view = toLenientIncidentProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientIncidentProposalViews(mutableListOf<LenientIncidentProposal>(item, item)).size, "views")
    }
}
