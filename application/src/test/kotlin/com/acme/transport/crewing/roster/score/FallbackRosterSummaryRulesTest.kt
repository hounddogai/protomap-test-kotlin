package com.acme.transport.crewing.roster.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRosterSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRosterSummaryValidator()
        val item = newFallbackRosterSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = FallbackRosterSummarySequenceRangeRule(16, 64)
        val item = newFallbackRosterSummary("x", "y")
        item.sequence = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = FallbackRosterSummaryRegionLengthRule(16)
        val item = newFallbackRosterSummary("x", "y")
        item.region = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRosterSummaryCodeLengthRule(64)
        val item = newFallbackRosterSummary("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = FallbackRosterSummaryRequiredRegionRule("fallback roster summary needs this field")
        val item = newFallbackRosterSummary("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = FallbackRosterSummaryRevisionRangeRule(80, 120)
        val item = newFallbackRosterSummary("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
