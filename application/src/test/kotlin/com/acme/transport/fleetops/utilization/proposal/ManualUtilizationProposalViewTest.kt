package com.acme.transport.fleetops.utilization.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualUtilizationProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualUtilizationProposal("code-342", "name-247")
        val view = toManualUtilizationProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualUtilizationProposalViews(mutableListOf<ManualUtilizationProposal>(item, item)).size, "views")
    }
}
