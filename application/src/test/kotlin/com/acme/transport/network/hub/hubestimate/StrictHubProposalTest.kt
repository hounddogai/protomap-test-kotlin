package com.acme.transport.network.hub.hubestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictHubProposalTest {
    @Test
    fun `starts in open`() {
        val item = newStrictHubProposal("code-727", "title-888")
        assertEquals(StrictHubProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newStrictHubProposal("code-727", "title-888")
        try {
            item.moveTo(StrictHubProposalStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictHubProposalStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictHubProposal("code-727", "title-888")
        item.status = StrictHubProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictHubProposalStatus.OPEN) }
    }
}
