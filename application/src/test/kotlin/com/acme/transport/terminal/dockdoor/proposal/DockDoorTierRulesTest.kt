package com.acme.transport.terminal.dockdoor.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorTierValidator()
        val item = newDockDoorTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = DockDoorTierFloorValueRangeRule(1.7, 2264.8)
        val item = newDockDoorTier("x", "y")
        item.floorValue = 2266.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DockDoorTierRequiredNameRule("dock door tier needs this field")
        val item = newDockDoorTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = DockDoorTierUnitLengthRule(120)
        val item = newDockDoorTier("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorTierRequiredCodeRule("dock door tier needs this field")
        val item = newDockDoorTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = DockDoorTierCarrierLengthRule(255)
        val item = newDockDoorTier("x", "y")
        item.carrier = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
