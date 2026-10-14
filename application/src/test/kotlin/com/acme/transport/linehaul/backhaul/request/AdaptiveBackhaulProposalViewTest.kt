package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveBackhaulProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveBackhaulProposal("code-694", "title-793")
        val view = toAdaptiveBackhaulProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveBackhaulProposalViews(mutableListOf<AdaptiveBackhaulProposal>(item, item)).size, "views")
    }
}
