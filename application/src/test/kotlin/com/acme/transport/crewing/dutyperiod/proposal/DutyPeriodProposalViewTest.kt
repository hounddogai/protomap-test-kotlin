package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodProposal("code-566", "title-46")
        val view = toDutyPeriodProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodProposalViews(mutableListOf<DutyPeriodProposal>(item, item)).size, "views")
    }
}
