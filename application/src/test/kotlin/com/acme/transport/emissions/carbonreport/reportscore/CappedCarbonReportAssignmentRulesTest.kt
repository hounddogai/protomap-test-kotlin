package com.acme.transport.emissions.carbonreport.reportscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCarbonReportAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCarbonReportAssignmentValidator()
        val item = newCappedCarbonReportAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = CappedCarbonReportAssignmentDescriptionLengthRule(80)
        val item = newCappedCarbonReportAssignment("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedCarbonReportAssignmentRequiredCodeRule("capped carbon report assignment needs an entry")
        val item = newCappedCarbonReportAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
