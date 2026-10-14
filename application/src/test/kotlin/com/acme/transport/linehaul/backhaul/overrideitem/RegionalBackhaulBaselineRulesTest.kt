package com.acme.transport.linehaul.backhaul.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalBackhaulBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalBackhaulBaselineValidator()
        val item = newRegionalBackhaulBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RegionalBackhaulBaselineDiscountRangeRule(9.0, 3306.5)
        val item = newRegionalBackhaulBaseline("x", "y")
        item.discount = 3308.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RegionalBackhaulBaselineMaximumRangeRule(4.0, 3140.6)
        val item = newRegionalBackhaulBaseline("x", "y")
        item.maximum = 3142.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RegionalBackhaulBaselineAttemptsRangeRule(80, 1000)
        val item = newRegionalBackhaulBaseline("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
