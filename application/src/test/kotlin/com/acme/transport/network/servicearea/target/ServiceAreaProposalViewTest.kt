package com.acme.transport.network.servicearea.target

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaProposal("code-227", "label-333")
        val view = toServiceAreaProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaProposalViews(mutableListOf<ServiceAreaProposal>(item, item)).size, "views")
    }
}
