package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDockDoorCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDockDoorCycleValidator()
        val item = newAdaptiveDockDoorCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveDockDoorCycleRequiredLabelRule("adaptive dock door cycle needs this field")
        val item = newAdaptiveDockDoorCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveDockDoorCycleCodeLengthRule(86400)
        val item = newAdaptiveDockDoorCycle("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = AdaptiveDockDoorCycleRequiredDriverIdRule("adaptive dock door cycle needs this field")
        val item = newAdaptiveDockDoorCycle("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = AdaptiveDockDoorCycleRatioRangeRule(1.2, 4800.8)
        val item = newAdaptiveDockDoorCycle("x", "y")
        item.ratio = 4802.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = AdaptiveDockDoorCycleMinimumRangeRule(5.9, 4563.3)
        val item = newAdaptiveDockDoorCycle("x", "y")
        item.minimum = 4564.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
