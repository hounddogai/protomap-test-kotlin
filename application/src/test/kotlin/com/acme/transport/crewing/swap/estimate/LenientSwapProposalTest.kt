package com.acme.transport.crewing.swap.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientSwapProposalTest {
    @Test
    fun `starts in open`() {
        val item = newLenientSwapProposal("code-348", "name-277")
        assertEquals(LenientSwapProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLenientSwapProposal("code-348", "name-277")
        try {
            item.moveTo(LenientSwapProposalStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientSwapProposalStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientSwapProposal("code-348", "name-277")
        item.status = LenientSwapProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientSwapProposalStatus.OPEN) }
    }
}
