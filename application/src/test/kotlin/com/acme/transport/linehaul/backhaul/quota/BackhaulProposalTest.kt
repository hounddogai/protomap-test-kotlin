package com.acme.transport.linehaul.backhaul.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newBackhaulProposal("code-3", "name-652")
        assertEquals(BackhaulProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newBackhaulProposal("code-3", "name-652")
        try {
            item.moveTo(BackhaulProposalStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulProposalStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBackhaulProposal("code-3", "name-652")
        item.status = BackhaulProposalStatus.CLOSED
        assertFails("move") { item.moveTo(BackhaulProposalStatus.DRAFT) }
    }
}
