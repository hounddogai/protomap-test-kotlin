package com.acme.transport.terminal.dockdoor.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newDockDoorRule("code-377", "name-185")
        assertEquals(DockDoorRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDockDoorRule("code-377", "name-185")
        try {
            item.moveTo(DockDoorRuleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorRuleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDockDoorRule("code-377", "name-185")
        item.status = DockDoorRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(DockDoorRuleStatus.REQUESTED) }
    }
}
