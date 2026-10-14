package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAccessorialProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientAccessorialProposal("code-374", "name-766")
        assertEquals(LenientAccessorialProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newLenientAccessorialProposal("code-374", "name-766")
        try {
            item.moveTo(LenientAccessorialProposalStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAccessorialProposalStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientAccessorialProposal("code-374", "name-766")
        item.status = LenientAccessorialProposalStatus.CLOSED
        assertFails("move") { item.moveTo(LenientAccessorialProposalStatus.DRAFT) }
    }
}
