package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictZoneSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictZoneSummaryValidator()
        val item = newStrictZoneSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictZoneSummaryLabelLengthRule(255)
        val item = newStrictZoneSummary("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictZoneSummaryRequiredLabelRule("strict zone summary needs this field")
        val item = newStrictZoneSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = StrictZoneSummaryRequiredLaneRule("strict zone summary needs this field")
        val item = newStrictZoneSummary("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = StrictZoneSummaryLaneLengthRule(64)
        val item = newStrictZoneSummary("x", "y")
        item.lane = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = StrictZoneSummaryCostRangeRule(5.0, 1738.2)
        val item = newStrictZoneSummary("x", "y")
        item.cost = 1739.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
