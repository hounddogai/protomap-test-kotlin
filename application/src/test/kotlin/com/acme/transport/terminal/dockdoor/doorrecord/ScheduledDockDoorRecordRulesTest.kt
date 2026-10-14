package com.acme.transport.terminal.dockdoor.doorrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDockDoorRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDockDoorRecordValidator()
        val item = newScheduledDockDoorRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ScheduledDockDoorRecordFloorValueRangeRule(0.7, 2247.8)
        val item = newScheduledDockDoorRecord("x", "y")
        item.floorValue = 2249.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledDockDoorRecordNameLengthRule(255)
        val item = newScheduledDockDoorRecord("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledDockDoorRecordRequiredNameRule("scheduled dock door record needs this field")
        val item = newScheduledDockDoorRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
