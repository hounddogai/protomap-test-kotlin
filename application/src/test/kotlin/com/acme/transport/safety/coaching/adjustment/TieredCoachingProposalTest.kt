package com.acme.transport.safety.coaching.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCoachingProposalTest {
    @Test
    fun `starts in new`() {
        val item = newTieredCoachingProposal("code-78", "name-55")
        assertEquals(TieredCoachingProposalStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newTieredCoachingProposal("code-78", "name-55")
        try {
            item.moveTo(TieredCoachingProposalStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCoachingProposalStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredCoachingProposal("code-78", "name-55")
        item.status = TieredCoachingProposalStatus.FAILED
        assertFails("move") { item.moveTo(TieredCoachingProposalStatus.NEW) }
    }
}
