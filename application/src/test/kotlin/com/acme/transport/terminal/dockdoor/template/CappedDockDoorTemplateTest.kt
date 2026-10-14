package com.acme.transport.terminal.dockdoor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDockDoorTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedDockDoorTemplate("code-499", "label-16")
        assertEquals(CappedDockDoorTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCappedDockDoorTemplate("code-499", "label-16")
        try {
            item.moveTo(CappedDockDoorTemplateStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDockDoorTemplateStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedDockDoorTemplate("code-499", "label-16")
        item.status = CappedDockDoorTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(CappedDockDoorTemplateStatus.DRAFT) }
    }
}
