package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultHubProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultHubProposal("code-581", "title-336")
        val view = toDefaultHubProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultHubProposalViews(mutableListOf<DefaultHubProposal>(item, item)).size, "views")
    }
}
