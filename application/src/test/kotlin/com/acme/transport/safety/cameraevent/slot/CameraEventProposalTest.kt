package com.acme.transport.safety.cameraevent.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newCameraEventProposal("code-561", "title-446")
        assertEquals(CameraEventProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCameraEventProposal("code-561", "title-446")
        try {
            item.moveTo(CameraEventProposalStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventProposalStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCameraEventProposal("code-561", "title-446")
        item.status = CameraEventProposalStatus.CLOSED
        assertFails("move") { item.moveTo(CameraEventProposalStatus.DRAFT) }
    }
}
