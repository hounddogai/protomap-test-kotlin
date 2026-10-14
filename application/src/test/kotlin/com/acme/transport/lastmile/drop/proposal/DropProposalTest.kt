package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropProposalTest {
    @Test
    fun `starts in open`() {
        val item = newDropProposal("code-753", "label-204")
        assertEquals(DropProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newDropProposal("code-753", "label-204")
        try {
            item.moveTo(DropProposalStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropProposalStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDropProposal("code-753", "label-204")
        item.status = DropProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(DropProposalStatus.OPEN) }
    }
}
