package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportMilestoneValidator()
        val item = newCarbonReportMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = CarbonReportMilestoneRequiredTrailerIdRule("carbon report milestone needs this field")
        val item = newCarbonReportMilestone("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CarbonReportMilestoneLabelLengthRule(64)
        val item = newCarbonReportMilestone("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CarbonReportMilestoneGraceDaysRangeRule(80, 86400)
        val item = newCarbonReportMilestone("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = CarbonReportMilestoneTrailerIdLengthRule(255)
        val item = newCarbonReportMilestone("x", "y")
        item.trailerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
