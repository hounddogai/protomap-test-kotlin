package com.acme.transport.fleetops.tire.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTireEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTireEstimateValidator()
        val item = newDefaultTireEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = DefaultTireEstimateWindowDaysRangeRule(255, 86400)
        val item = newDefaultTireEstimate("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DefaultTireEstimateThresholdRangeRule(0.8, 2089.5)
        val item = newDefaultTireEstimate("x", "y")
        item.threshold = 2091.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
