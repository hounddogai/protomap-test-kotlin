package com.acme.transport.terminal.dockdoor.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDockDoorAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingDockDoorAgreement("code-68", "label-518")
        assertEquals(PendingDockDoorAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPendingDockDoorAgreement("code-68", "label-518")
        try {
            item.moveTo(PendingDockDoorAgreementStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDockDoorAgreementStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingDockDoorAgreement("code-68", "label-518")
        item.status = PendingDockDoorAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(PendingDockDoorAgreementStatus.DRAFT) }
    }
}
