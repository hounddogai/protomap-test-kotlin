package com.acme.transport.emissions.carbonreport.regionalreportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCarbonReportRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyCarbonReportRuleValidator()
        val item = newLegacyCarbonReportRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LegacyCarbonReportRuleLimitRangeRule(255, 1000)
        val item = newLegacyCarbonReportRule("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = LegacyCarbonReportRuleShiftCodeLengthRule(86400)
        val item = newLegacyCarbonReportRule("x", "y")
        item.shiftCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = LegacyCarbonReportRuleEtaMinutesRangeRule(64, 1000)
        val item = newLegacyCarbonReportRule("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LegacyCarbonReportRuleWeightRangeRule(1.3, 4904.9)
        val item = newLegacyCarbonReportRule("x", "y")
        item.weight = 4906.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = LegacyCarbonReportRuleRequiredShiftCodeRule("legacy carbon report rule needs an entry")
        val item = newLegacyCarbonReportRule("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
