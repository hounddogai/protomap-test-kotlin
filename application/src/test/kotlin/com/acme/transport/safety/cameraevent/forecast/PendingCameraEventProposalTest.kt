package com.acme.transport.safety.cameraevent.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCameraEventProposalTest {
    @Test
    fun `starts in created`() {
        val item = newPendingCameraEventProposal("code-197", "name-683")
        assertEquals(PendingCameraEventProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPendingCameraEventProposal("code-197", "name-683")
        try {
            item.moveTo(PendingCameraEventProposalStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCameraEventProposalStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingCameraEventProposal("code-197", "name-683")
        item.status = PendingCameraEventProposalStatus.RETIRED
        assertFails("move") { item.moveTo(PendingCameraEventProposalStatus.CREATED) }
    }
}
