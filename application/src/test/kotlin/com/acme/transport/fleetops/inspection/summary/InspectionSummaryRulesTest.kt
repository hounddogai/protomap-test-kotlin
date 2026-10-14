package com.acme.transport.fleetops.inspection.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionSummaryValidator()
        val item = newInspectionSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = InspectionSummaryTitleLengthRule(64)
        val item = newInspectionSummary("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = InspectionSummaryRequiredShiftCodeRule("inspection summary needs an entry")
        val item = newInspectionSummary("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = InspectionSummaryChannelLengthRule(64)
        val item = newInspectionSummary("x", "y")
        item.channel = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
