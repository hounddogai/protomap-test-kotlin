package com.acme.transport.crewing.restbreak.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newRestBreakProposal("code-122", "label-989")
        assertEquals(RestBreakProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRestBreakProposal("code-122", "label-989")
        try {
            item.moveTo(RestBreakProposalStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakProposalStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRestBreakProposal("code-122", "label-989")
        item.status = RestBreakProposalStatus.STOPPED
        assertFails("move") { item.moveTo(RestBreakProposalStatus.IDLE) }
    }
}
