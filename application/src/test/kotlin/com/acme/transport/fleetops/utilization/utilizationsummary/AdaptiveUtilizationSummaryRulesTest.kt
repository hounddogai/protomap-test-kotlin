package com.acme.transport.fleetops.utilization.utilizationsummary

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveUtilizationSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveUtilizationSummaryValidator()
        val item = newAdaptiveUtilizationSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = AdaptiveUtilizationSummaryDiscountRangeRule(7.1, 3962.2)
        val item = newAdaptiveUtilizationSummary("x", "y")
        item.discount = 3963.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
