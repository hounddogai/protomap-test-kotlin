package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newDeclarationProposal("code-897", "name-594")
        assertEquals(DeclarationProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDeclarationProposal("code-897", "name-594")
        try {
            item.moveTo(DeclarationProposalStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationProposalStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDeclarationProposal("code-897", "name-594")
        item.status = DeclarationProposalStatus.STOPPED
        assertFails("move") { item.moveTo(DeclarationProposalStatus.IDLE) }
    }
}
