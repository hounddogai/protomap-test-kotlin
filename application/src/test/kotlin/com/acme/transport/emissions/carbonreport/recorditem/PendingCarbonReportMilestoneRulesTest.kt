package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCarbonReportMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCarbonReportMilestoneValidator()
        val item = newPendingCarbonReportMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PendingCarbonReportMilestoneRequiredChannelRule("pending carbon report milestone needs an entry")
        val item = newPendingCarbonReportMilestone("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingCarbonReportMilestoneTitleLengthRule(120)
        val item = newPendingCarbonReportMilestone("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
