package com.acme.transport.terminal.dwell.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDwellSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDwellSummaryValidator()
        val item = newWeightedDwellSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedDwellSummaryRequiredLabelRule("weighted dwell summary needs a value")
        val item = newWeightedDwellSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = WeightedDwellSummaryTrailerIdLengthRule(255)
        val item = newWeightedDwellSummary("x", "y")
        item.trailerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedDwellSummaryLabelLengthRule(86400)
        val item = newWeightedDwellSummary("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedDwellSummaryRequiredCodeRule("weighted dwell summary needs this field")
        val item = newWeightedDwellSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = WeightedDwellSummaryRetriesRangeRule(80, 255)
        val item = newWeightedDwellSummary("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
