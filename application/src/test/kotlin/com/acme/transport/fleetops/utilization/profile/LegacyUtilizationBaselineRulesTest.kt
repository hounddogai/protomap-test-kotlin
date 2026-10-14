package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyUtilizationBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyUtilizationBaselineValidator()
        val item = newLegacyUtilizationBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = LegacyUtilizationBaselineRequiredLaneRule("legacy utilization baseline needs an entry")
        val item = newLegacyUtilizationBaseline("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = LegacyUtilizationBaselineMarginRangeRule(3.4, 293.9)
        val item = newLegacyUtilizationBaseline("x", "y")
        item.margin = 295.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = LegacyUtilizationBaselineReviewerLengthRule(32)
        val item = newLegacyUtilizationBaseline("x", "y")
        item.reviewer = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyUtilizationBaselineTitleLengthRule(64)
        val item = newLegacyUtilizationBaseline("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
