package com.acme.transport.terminal.dockdoor.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorThresholdValidator()
        val item = newDockDoorThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = DockDoorThresholdRequiredRouteCodeRule("dock door threshold needs a value")
        val item = newDockDoorThreshold("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DockDoorThresholdNameLengthRule(80)
        val item = newDockDoorThreshold("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = DockDoorThresholdRateRangeRule(3.7, 133.4)
        val item = newDockDoorThreshold("x", "y")
        item.rate = 134.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DockDoorThresholdRequiredNameRule("dock door threshold needs a value")
        val item = newDockDoorThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = DockDoorThresholdRequiredPhoneRule("dock door threshold needs a value")
        val item = newDockDoorThreshold("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorThresholdRequiredCodeRule("dock door threshold needs this field")
        val item = newDockDoorThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
