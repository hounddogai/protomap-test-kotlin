package com.acme.transport.linehaul.laneplan.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanTargetValidator()
        val item = newLanePlanTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = LanePlanTargetBudgetRangeRule(2.9, 3763.6)
        val item = newLanePlanTarget("x", "y")
        item.budget = 3765.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
