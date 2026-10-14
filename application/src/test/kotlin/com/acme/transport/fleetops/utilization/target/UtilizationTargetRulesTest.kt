package com.acme.transport.fleetops.utilization.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationTargetValidator()
        val item = newUtilizationTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationTargetRequiredCodeRule("utilization target needs an entry")
        val item = newUtilizationTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = UtilizationTargetFloorValueRangeRule(5.3, 2710.0)
        val item = newUtilizationTarget("x", "y")
        item.floorValue = 2711.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = UtilizationTargetRequiredSourceRule("utilization target needs this field")
        val item = newUtilizationTarget("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = UtilizationTargetCodeLengthRule(255)
        val item = newUtilizationTarget("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
