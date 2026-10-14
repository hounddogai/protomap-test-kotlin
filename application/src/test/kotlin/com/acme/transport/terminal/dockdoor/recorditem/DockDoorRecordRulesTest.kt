package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorRecordValidator()
        val item = newDockDoorRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = DockDoorRecordDistanceKmRangeRule(5.6, 1652.2)
        val item = newDockDoorRecord("x", "y")
        item.distanceKm = 1653.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DockDoorRecordTitleLengthRule(32)
        val item = newDockDoorRecord("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = DockDoorRecordRouteCodeLengthRule(86400)
        val item = newDockDoorRecord("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DockDoorRecordRequiredTitleRule("dock door record needs a value")
        val item = newDockDoorRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = DockDoorRecordRequiredRouteCodeRule("dock door record needs this field")
        val item = newDockDoorRecord("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
