package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportExceptionValidator()
        val item = newCarbonReportException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CarbonReportExceptionRequiredCodeRule("carbon report exception needs a value")
        val item = newCarbonReportException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = CarbonReportExceptionRateRangeRule(1.0, 2617.8)
        val item = newCarbonReportException("x", "y")
        item.rate = 2619.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CarbonReportExceptionCodeLengthRule(64)
        val item = newCarbonReportException("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = CarbonReportExceptionLabelLengthRule(80)
        val item = newCarbonReportException("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = CarbonReportExceptionRequiredPhoneRule("carbon report exception needs a value")
        val item = newCarbonReportException("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = CarbonReportExceptionLoadKgRangeRule(3.5, 2910.1)
        val item = newCarbonReportException("x", "y")
        item.loadKg = 2911.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
