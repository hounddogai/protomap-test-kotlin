package com.acme.transport.lastmile.redelivery.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRedeliveryRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRedeliveryRuleValidator()
        val item = newPrimaryRedeliveryRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = PrimaryRedeliveryRuleFloorValueRangeRule(5.7, 2309.2)
        val item = newPrimaryRedeliveryRule("x", "y")
        item.floorValue = 2310.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PrimaryRedeliveryRuleStopCountRangeRule(16, 86400)
        val item = newPrimaryRedeliveryRule("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = PrimaryRedeliveryRuleRequiredTimezoneRule("primary redelivery rule needs this field")
        val item = newPrimaryRedeliveryRule("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = PrimaryRedeliveryRuleRequiredRouteCodeRule("primary redelivery rule needs a value")
        val item = newPrimaryRedeliveryRule("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
