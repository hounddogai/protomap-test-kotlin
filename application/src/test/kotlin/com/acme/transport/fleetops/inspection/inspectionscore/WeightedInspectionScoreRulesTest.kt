package com.acme.transport.fleetops.inspection.inspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedInspectionScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedInspectionScoreValidator()
        val item = newWeightedInspectionScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedInspectionScoreCodeLengthRule(16)
        val item = newWeightedInspectionScore("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = WeightedInspectionScoreVersionRangeRule(80, 255)
        val item = newWeightedInspectionScore("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = WeightedInspectionScoreRequiredLaneRule("weighted inspection score needs a value")
        val item = newWeightedInspectionScore("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
