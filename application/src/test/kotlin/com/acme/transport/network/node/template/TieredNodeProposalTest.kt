package com.acme.transport.network.node.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredNodeProposalTest {
    @Test
    fun `starts in open`() {
        val item = newTieredNodeProposal("code-273", "label-760")
        assertEquals(TieredNodeProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newTieredNodeProposal("code-273", "label-760")
        try {
            item.moveTo(TieredNodeProposalStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredNodeProposalStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredNodeProposal("code-273", "label-760")
        item.status = TieredNodeProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredNodeProposalStatus.OPEN) }
    }
}
