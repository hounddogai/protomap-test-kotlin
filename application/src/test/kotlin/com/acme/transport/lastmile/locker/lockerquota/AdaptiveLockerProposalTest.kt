package com.acme.transport.lastmile.locker.lockerquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLockerProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveLockerProposal("code-512", "name-535")
        assertEquals(AdaptiveLockerProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAdaptiveLockerProposal("code-512", "name-535")
        try {
            item.moveTo(AdaptiveLockerProposalStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLockerProposalStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveLockerProposal("code-512", "name-535")
        item.status = AdaptiveLockerProposalStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveLockerProposalStatus.IDLE) }
    }
}
