package com.acme.transport.lastmile.locker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerProposalTest {
    @Test
    fun `starts in created`() {
        val item = newLockerProposal("code-588", "title-423")
        assertEquals(LockerProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLockerProposal("code-588", "title-423")
        try {
            item.moveTo(LockerProposalStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerProposalStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLockerProposal("code-588", "title-423")
        item.status = LockerProposalStatus.RETIRED
        assertFails("move") { item.moveTo(LockerProposalStatus.CREATED) }
    }
}
