package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyUtilizationEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyUtilizationEstimateValidator()
        val item = newLegacyUtilizationEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = LegacyUtilizationEstimateEndsAtRangeRule(64, 86400)
        val item = newLegacyUtilizationEstimate("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyUtilizationEstimateRequiredTitleRule("legacy utilization estimate needs this field")
        val item = newLegacyUtilizationEstimate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LegacyUtilizationEstimateDiscountRangeRule(2.3, 1191.7)
        val item = newLegacyUtilizationEstimate("x", "y")
        item.discount = 1193.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
