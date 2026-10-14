package com.acme.transport.customs.broker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerProposalTest {
    @Test
    fun `starts in open`() {
        val item = newBrokerProposal("code-605", "title-440")
        assertEquals(BrokerProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newBrokerProposal("code-605", "title-440")
        try {
            item.moveTo(BrokerProposalStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerProposalStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBrokerProposal("code-605", "title-440")
        item.status = BrokerProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(BrokerProposalStatus.OPEN) }
    }
}
