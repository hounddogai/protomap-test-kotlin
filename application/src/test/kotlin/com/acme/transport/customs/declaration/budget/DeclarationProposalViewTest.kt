package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationProposal("code-897", "name-594")
        val view = toDeclarationProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationProposalViews(mutableListOf<DeclarationProposal>(item, item)).size, "views")
    }
}
