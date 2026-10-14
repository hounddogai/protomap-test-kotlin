package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultHubProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultHubProposal("code-581", "title-336")
        assertEquals(DefaultHubProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDefaultHubProposal("code-581", "title-336")
        try {
            item.moveTo(DefaultHubProposalStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultHubProposalStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultHubProposal("code-581", "title-336")
        item.status = DefaultHubProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultHubProposalStatus.REQUESTED) }
    }
}
