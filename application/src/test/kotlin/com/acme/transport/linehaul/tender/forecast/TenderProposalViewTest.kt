package com.acme.transport.linehaul.tender.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderProposal("code-788", "label-613")
        val view = toTenderProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderProposalViews(mutableListOf<TenderProposal>(item, item)).size, "views")
    }
}
