package com.acme.transport.fleetops.defect.defectproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDefectProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDefectProposal("code-681", "name-402")
        val view = toRollingDefectProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDefectProposalViews(mutableListOf<RollingDefectProposal>(item, item)).size, "views")
    }
}
