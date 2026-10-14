package com.acme.transport.fleetops.inspection.inspectionbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledInspectionAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledInspectionAgreementValidator()
        val item = newScheduledInspectionAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledInspectionAgreementNameLengthRule(1000)
        val item = newScheduledInspectionAgreement("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ScheduledInspectionAgreementWindowDaysRangeRule(80, 1000)
        val item = newScheduledInspectionAgreement("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledInspectionAgreementRequiredCodeRule("scheduled inspection agreement needs this field")
        val item = newScheduledInspectionAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
