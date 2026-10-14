package com.acme.transport.customs.clearance.clearancecheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceProposal("code-469", "title-636")
        val view = toClearanceProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceProposalViews(mutableListOf<ClearanceProposal>(item, item)).size, "views")
    }
}
