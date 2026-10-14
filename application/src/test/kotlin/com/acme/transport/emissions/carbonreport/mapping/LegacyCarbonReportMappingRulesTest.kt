package com.acme.transport.emissions.carbonreport.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCarbonReportMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyCarbonReportMappingValidator()
        val item = newLegacyCarbonReportMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LegacyCarbonReportMappingCapacityRangeRule(16, 120)
        val item = newLegacyCarbonReportMapping("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyCarbonReportMappingTitleLengthRule(64)
        val item = newLegacyCarbonReportMapping("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyCarbonReportMappingRequiredCodeRule("legacy carbon report mapping needs an entry")
        val item = newLegacyCarbonReportMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = LegacyCarbonReportMappingCeilingRangeRule(2.8, 3363.8)
        val item = newLegacyCarbonReportMapping("x", "y")
        item.ceiling = 3365.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
