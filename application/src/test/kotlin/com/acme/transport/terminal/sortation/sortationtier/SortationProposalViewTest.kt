package com.acme.transport.terminal.sortation.sortationtier

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationProposal("code-221", "title-779")
        val view = toSortationProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationProposalViews(mutableListOf<SortationProposal>(item, item)).size, "views")
    }
}
