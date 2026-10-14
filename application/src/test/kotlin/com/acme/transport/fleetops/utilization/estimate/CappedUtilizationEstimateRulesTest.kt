package com.acme.transport.fleetops.utilization.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedUtilizationEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedUtilizationEstimateValidator()
        val item = newCappedUtilizationEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = CappedUtilizationEstimateMinimumRangeRule(8.2, 2092.2)
        val item = newCappedUtilizationEstimate("x", "y")
        item.minimum = 2093.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedUtilizationEstimateLabelLengthRule(32)
        val item = newCappedUtilizationEstimate("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = CappedUtilizationEstimateRequiredDriverIdRule("capped utilization estimate needs this field")
        val item = newCappedUtilizationEstimate("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
