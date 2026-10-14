package com.acme.transport.network.zone.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneProposal("code-665", "title-915")
        val view = toZoneProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneProposalViews(mutableListOf<ZoneProposal>(item, item)).size, "views")
    }
}
