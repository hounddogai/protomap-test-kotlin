package com.acme.transport.fleetops.inspection.group

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionProposal("code-824", "title-459")
        val view = toInspectionProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionProposalViews(mutableListOf<InspectionProposal>(item, item)).size, "views")
    }
}
