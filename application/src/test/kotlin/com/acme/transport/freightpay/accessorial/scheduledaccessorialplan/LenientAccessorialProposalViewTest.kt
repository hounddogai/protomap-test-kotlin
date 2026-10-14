package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientAccessorialProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientAccessorialProposal("code-374", "name-766")
        val view = toLenientAccessorialProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientAccessorialProposalViews(mutableListOf<LenientAccessorialProposal>(item, item)).size, "views")
    }
}
