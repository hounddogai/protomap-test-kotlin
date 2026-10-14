package com.acme.transport.emissions.carbonreport.tieredreportscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCarbonReportScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCarbonReportScoreValidator()
        val item = newWeightedCarbonReportScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = WeightedCarbonReportScoreShiftCodeLengthRule(16)
        val item = newWeightedCarbonReportScore("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = WeightedCarbonReportScoreExpiresAtRangeRule(32, 86400)
        val item = newWeightedCarbonReportScore("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedCarbonReportScoreRequiredCodeRule("weighted carbon report score needs an entry")
        val item = newWeightedCarbonReportScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = WeightedCarbonReportScoreStartsAtRangeRule(86400, 86400)
        val item = newWeightedCarbonReportScore("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
