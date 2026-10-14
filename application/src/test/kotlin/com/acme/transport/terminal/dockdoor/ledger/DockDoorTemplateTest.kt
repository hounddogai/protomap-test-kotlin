package com.acme.transport.terminal.dockdoor.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newDockDoorTemplate("code-754", "label-731")
        assertEquals(DockDoorTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDockDoorTemplate("code-754", "label-731")
        try {
            item.moveTo(DockDoorTemplateStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorTemplateStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDockDoorTemplate("code-754", "label-731")
        item.status = DockDoorTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(DockDoorTemplateStatus.DRAFT) }
    }
}
