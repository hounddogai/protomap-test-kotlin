package com.acme.transport.terminal.sortation.sortationtier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationProposalTest {
    @Test
    fun `starts in created`() {
        val item = newSortationProposal("code-221", "title-779")
        assertEquals(SortationProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSortationProposal("code-221", "title-779")
        try {
            item.moveTo(SortationProposalStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationProposalStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSortationProposal("code-221", "title-779")
        item.status = SortationProposalStatus.RETIRED
        assertFails("move") { item.moveTo(SortationProposalStatus.CREATED) }
    }
}
