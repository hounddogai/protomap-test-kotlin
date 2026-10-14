package com.acme.transport.crewing.swap.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientSwapProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientSwapProposal("code-348", "name-277")
        val view = toLenientSwapProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientSwapProposalViews(mutableListOf<LenientSwapProposal>(item, item)).size, "views")
    }
}
