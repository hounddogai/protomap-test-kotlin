package com.acme.transport.lastmile.parcel.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualParcelProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualParcelProposal("code-884", "label-413")
        val view = toManualParcelProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualParcelProposalViews(mutableListOf<ManualParcelProposal>(item, item)).size, "views")
    }
}
