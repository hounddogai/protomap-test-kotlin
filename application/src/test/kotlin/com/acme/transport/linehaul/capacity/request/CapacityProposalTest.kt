package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityProposalTest {
    @Test
    fun `starts in new`() {
        val item = newCapacityProposal("code-204", "name-442")
        assertEquals(CapacityProposalStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCapacityProposal("code-204", "name-442")
        try {
            item.moveTo(CapacityProposalStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityProposalStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCapacityProposal("code-204", "name-442")
        item.status = CapacityProposalStatus.FAILED
        assertFails("move") { item.moveTo(CapacityProposalStatus.NEW) }
    }
}
