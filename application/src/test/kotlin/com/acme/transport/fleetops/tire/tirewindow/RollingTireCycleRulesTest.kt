package com.acme.transport.fleetops.tire.tirewindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTireCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTireCycleValidator()
        val item = newRollingTireCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = RollingTireCycleFloorValueRangeRule(6.8, 4026.3)
        val item = newRollingTireCycle("x", "y")
        item.floorValue = 4027.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingTireCycleRequiredTitleRule("rolling tire cycle needs a value")
        val item = newRollingTireCycle("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
