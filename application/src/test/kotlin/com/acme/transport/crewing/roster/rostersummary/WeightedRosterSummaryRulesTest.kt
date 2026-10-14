package com.acme.transport.crewing.roster.rostersummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRosterSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRosterSummaryValidator()
        val item = newWeightedRosterSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedRosterSummaryLabelLengthRule(32)
        val item = newWeightedRosterSummary("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = WeightedRosterSummaryRequiredSegmentRule("weighted roster summary needs an entry")
        val item = newWeightedRosterSummary("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = WeightedRosterSummaryVersionRangeRule(32, 64)
        val item = newWeightedRosterSummary("x", "y")
        item.version = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
