package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorAgreementTest {
    @Test
    fun `starts in proposed`() {
        val item = newDockDoorAgreement("code-398", "name-983")
        assertEquals(DockDoorAgreementStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDockDoorAgreement("code-398", "name-983")
        try {
            item.moveTo(DockDoorAgreementStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorAgreementStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDockDoorAgreement("code-398", "name-983")
        item.status = DockDoorAgreementStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DockDoorAgreementStatus.PROPOSED) }
    }
}
