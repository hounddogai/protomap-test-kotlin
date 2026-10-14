package com.acme.transport.customs.clearance.clearancecheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceProposalTest {
    @Test
    fun `starts in created`() {
        val item = newClearanceProposal("code-469", "title-636")
        assertEquals(ClearanceProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newClearanceProposal("code-469", "title-636")
        try {
            item.moveTo(ClearanceProposalStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceProposalStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newClearanceProposal("code-469", "title-636")
        item.status = ClearanceProposalStatus.RETIRED
        assertFails("move") { item.moveTo(ClearanceProposalStatus.CREATED) }
    }
}
