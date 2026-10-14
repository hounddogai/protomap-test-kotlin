package com.acme.transport.terminal.dockdoor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDockDoorTemplateTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalDockDoorTemplate("code-790", "name-360")
        assertEquals(GlobalDockDoorTemplateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newGlobalDockDoorTemplate("code-790", "name-360")
        try {
            item.moveTo(GlobalDockDoorTemplateStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDockDoorTemplateStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalDockDoorTemplate("code-790", "name-360")
        item.status = GlobalDockDoorTemplateStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalDockDoorTemplateStatus.OPEN) }
    }
}
