package com.acme.transport.lastmile.locker.lockerquota

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveLockerProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveLockerProposal("code-512", "name-535")
        val view = toAdaptiveLockerProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveLockerProposalViews(mutableListOf<AdaptiveLockerProposal>(item, item)).size, "views")
    }
}
