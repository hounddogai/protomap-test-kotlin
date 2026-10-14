package com.acme.transport.linehaul.laneplan.planrule

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanProposal("code-717", "title-867")
        val view = toLanePlanProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanProposalViews(mutableListOf<LanePlanProposal>(item, item)).size, "views")
    }
}
