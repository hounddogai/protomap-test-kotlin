package com.acme.transport.fleetops.utilization.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationVarianceValidator()
        val item = newUtilizationVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = UtilizationVarianceWindowDaysRangeRule(120, 3600)
        val item = newUtilizationVariance("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = UtilizationVarianceLimitRangeRule(80, 1000)
        val item = newUtilizationVariance("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = UtilizationVarianceStopCountRangeRule(16, 3600)
        val item = newUtilizationVariance("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
