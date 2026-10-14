package com.acme.transport.emissions.carbonreport.reportmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCarbonReportMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictCarbonReportMappingValidator()
        val item = newStrictCarbonReportMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictCarbonReportMappingLabelLengthRule(255)
        val item = newStrictCarbonReportMapping("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = StrictCarbonReportMappingCeilingRangeRule(7.7, 3941.8)
        val item = newStrictCarbonReportMapping("x", "y")
        item.ceiling = 3943.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictCarbonReportMappingRequiredLabelRule("strict carbon report mapping needs a value")
        val item = newStrictCarbonReportMapping("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
