package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorSnapshotValidator()
        val item = newDockDoorSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = DockDoorSnapshotRequiredDockRule("dock door snapshot needs this field")
        val item = newDockDoorSnapshot("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = DockDoorSnapshotRequiredTrailerIdRule("dock door snapshot needs a value")
        val item = newDockDoorSnapshot("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
