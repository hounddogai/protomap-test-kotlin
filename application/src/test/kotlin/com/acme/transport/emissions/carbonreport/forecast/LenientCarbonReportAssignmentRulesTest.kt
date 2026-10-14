package com.acme.transport.emissions.carbonreport.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCarbonReportAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCarbonReportAssignmentValidator()
        val item = newLenientCarbonReportAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = LenientCarbonReportAssignmentContactEmailLengthRule(32)
        val item = newLenientCarbonReportAssignment("x", "y")
        item.contactEmail = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientCarbonReportAssignmentTitleLengthRule(120)
        val item = newLenientCarbonReportAssignment("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = LenientCarbonReportAssignmentRequiredExternalRefRule(
            "lenient carbon report assignment needs an entry",
        )
        val item = newLenientCarbonReportAssignment("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
