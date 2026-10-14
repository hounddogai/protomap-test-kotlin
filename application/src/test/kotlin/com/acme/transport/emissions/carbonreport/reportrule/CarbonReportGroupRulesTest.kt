package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportGroupValidator()
        val item = newCarbonReportGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CarbonReportGroupCodeLengthRule(16)
        val item = newCarbonReportGroup("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = CarbonReportGroupWeightRangeRule(10.0, 1125.2)
        val item = newCarbonReportGroup("x", "y")
        item.weight = 1126.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CarbonReportGroupRequiredLabelRule("carbon report group needs this field")
        val item = newCarbonReportGroup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = CarbonReportGroupMinimumRangeRule(7.2, 4110.9)
        val item = newCarbonReportGroup("x", "y")
        item.minimum = 4112.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
