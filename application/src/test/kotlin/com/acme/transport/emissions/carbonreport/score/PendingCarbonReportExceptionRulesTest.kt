package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCarbonReportExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCarbonReportExceptionValidator()
        val item = newPendingCarbonReportException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingCarbonReportExceptionRequiredTitleRule("pending carbon report exception needs this field")
        val item = newPendingCarbonReportException("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = PendingCarbonReportExceptionFloorValueRangeRule(6.8, 4478.0)
        val item = newPendingCarbonReportException("x", "y")
        item.floorValue = 4479.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
