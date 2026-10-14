package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveIncidentProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveIncidentProposal("code-315", "label-211")
        val view = toAdaptiveIncidentProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveIncidentProposalViews(mutableListOf<AdaptiveIncidentProposal>(item, item)).size, "views")
    }
}
