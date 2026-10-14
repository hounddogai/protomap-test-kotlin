package com.acme.transport.customs.broker.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerProposal("code-605", "title-440")
        val view = toBrokerProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerProposalViews(mutableListOf<BrokerProposal>(item, item)).size, "views")
    }
}
