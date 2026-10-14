package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newParcelProposal("code-691", "title-298")
        assertEquals(ParcelProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newParcelProposal("code-691", "title-298")
        try {
            item.moveTo(ParcelProposalStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelProposalStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newParcelProposal("code-691", "title-298")
        item.status = ParcelProposalStatus.CLOSED
        assertFails("move") { item.moveTo(ParcelProposalStatus.DRAFT) }
    }
}
