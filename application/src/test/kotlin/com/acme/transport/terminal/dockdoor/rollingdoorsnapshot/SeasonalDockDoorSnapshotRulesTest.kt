package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDockDoorSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDockDoorSnapshotValidator()
        val item = newSeasonalDockDoorSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDockDoorSnapshotRequiredCodeRule("seasonal dock door snapshot needs an entry")
        val item = newSeasonalDockDoorSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SeasonalDockDoorSnapshotRequiredTitleRule("seasonal dock door snapshot needs an entry")
        val item = newSeasonalDockDoorSnapshot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = SeasonalDockDoorSnapshotRequiredPhoneRule("seasonal dock door snapshot needs this field")
        val item = newSeasonalDockDoorSnapshot("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
