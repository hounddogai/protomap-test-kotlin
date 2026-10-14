package com.acme.transport.crewing.dutyperiod.request

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDutyPeriodProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDutyPeriodProposal("code-348", "name-321")
        val view = toLenientDutyPeriodProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDutyPeriodProposalViews(mutableListOf<LenientDutyPeriodProposal>(item, item)).size, "views")
    }
}
