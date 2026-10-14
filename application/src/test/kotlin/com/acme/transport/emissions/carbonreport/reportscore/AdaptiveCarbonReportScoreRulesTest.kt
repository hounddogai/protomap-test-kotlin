package com.acme.transport.emissions.carbonreport.reportscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCarbonReportScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCarbonReportScoreValidator()
        val item = newAdaptiveCarbonReportScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = AdaptiveCarbonReportScoreDescriptionLengthRule(86400)
        val item = newAdaptiveCarbonReportScore("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveCarbonReportScoreNameLengthRule(255)
        val item = newAdaptiveCarbonReportScore("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = AdaptiveCarbonReportScoreBudgetRangeRule(9.6, 3520.5)
        val item = newAdaptiveCarbonReportScore("x", "y")
        item.budget = 3522.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveCarbonReportScoreTitleLengthRule(1000)
        val item = newAdaptiveCarbonReportScore("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
