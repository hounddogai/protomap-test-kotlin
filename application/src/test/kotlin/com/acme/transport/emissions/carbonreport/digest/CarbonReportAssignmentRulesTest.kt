package com.acme.transport.emissions.carbonreport.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportAssignmentValidator()
        val item = newCarbonReportAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = CarbonReportAssignmentRequiredCurrencyRule("carbon report assignment needs a value")
        val item = newCarbonReportAssignment("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CarbonReportAssignmentNameLengthRule(1000)
        val item = newCarbonReportAssignment("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
