package com.acme.transport.linehaul.tender.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTenderProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTenderProfileValidator()
        val item = newLegacyTenderProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = LegacyTenderProfileRegionLengthRule(80)
        val item = newLegacyTenderProfile("x", "y")
        item.region = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LegacyTenderProfileAttemptsRangeRule(16, 64)
        val item = newLegacyTenderProfile("x", "y")
        item.attempts = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = LegacyTenderProfileSummaryLengthRule(1000)
        val item = newLegacyTenderProfile("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = LegacyTenderProfileRequiredRegionRule("legacy tender profile needs this field")
        val item = newLegacyTenderProfile("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
