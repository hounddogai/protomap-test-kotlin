package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialProposalTest {
    @Test
    fun `starts in proposed`() {
        val item = newAccessorialProposal("code-171", "label-843")
        assertEquals(AccessorialProposalStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAccessorialProposal("code-171", "label-843")
        try {
            item.moveTo(AccessorialProposalStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialProposalStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAccessorialProposal("code-171", "label-843")
        item.status = AccessorialProposalStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AccessorialProposalStatus.PROPOSED) }
    }
}
