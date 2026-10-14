package com.acme.transport.lastmile.parcel.cappedparcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientParcelProposalTest {
    @Test
    fun `starts in created`() {
        val item = newLenientParcelProposal("code-20", "name-597")
        assertEquals(LenientParcelProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientParcelProposal("code-20", "name-597")
        try {
            item.moveTo(LenientParcelProposalStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientParcelProposalStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientParcelProposal("code-20", "name-597")
        item.status = LenientParcelProposalStatus.RETIRED
        assertFails("move") { item.moveTo(LenientParcelProposalStatus.CREATED) }
    }
}
