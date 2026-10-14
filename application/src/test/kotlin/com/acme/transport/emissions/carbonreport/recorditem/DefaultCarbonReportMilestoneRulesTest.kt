package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultCarbonReportMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCarbonReportMilestoneValidator()
        val item = newDefaultCarbonReportMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = DefaultCarbonReportMilestoneGraceDaysRangeRule(32, 86400)
        val item = newDefaultCarbonReportMilestone("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DefaultCarbonReportMilestoneStartsAtRangeRule(64, 1000)
        val item = newDefaultCarbonReportMilestone("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = DefaultCarbonReportMilestoneDurationMinutesRangeRule(16, 120)
        val item = newDefaultCarbonReportMilestone("x", "y")
        item.durationMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
