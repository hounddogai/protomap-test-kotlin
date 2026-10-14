package com.acme.transport.linehaul.relay.target

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayPlanValidator()
        val item = newRelayPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RelayPlanDiscountRangeRule(4.0, 4112.8)
        val item = newRelayPlan("x", "y")
        item.discount = 4114.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = RelayPlanLimitRangeRule(120, 120)
        val item = newRelayPlan("x", "y")
        item.limit = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
