package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeProposalTest {
    @Test
    fun `starts in created`() {
        val item = newNodeProposal("code-748", "label-784")
        assertEquals(NodeProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newNodeProposal("code-748", "label-784")
        try {
            item.moveTo(NodeProposalStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeProposalStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newNodeProposal("code-748", "label-784")
        item.status = NodeProposalStatus.RETIRED
        assertFails("move") { item.moveTo(NodeProposalStatus.CREATED) }
    }
}
