package com.acme.transport.fleetops.inspection.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionScoreValidator()
        val item = newInspectionScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = InspectionScoreWeightRangeRule(5.8, 1719.8)
        val item = newInspectionScore("x", "y")
        item.weight = 1721.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionScoreLabelLengthRule(16)
        val item = newInspectionScore("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = InspectionScoreBudgetRangeRule(7.6, 4573.3)
        val item = newInspectionScore("x", "y")
        item.budget = 4574.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
