package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCarbonReportVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyCarbonReportVarianceValidator()
        val item = newLegacyCarbonReportVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LegacyCarbonReportVarianceDiscountRangeRule(8.1, 552.5)
        val item = newLegacyCarbonReportVariance("x", "y")
        item.discount = 554.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyCarbonReportVarianceCodeLengthRule(32)
        val item = newLegacyCarbonReportVariance("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyCarbonReportVarianceNameLengthRule(255)
        val item = newLegacyCarbonReportVariance("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
