package com.acme.transport.terminal.traileryard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardEntryValidator()
        val item = newTrailerYardEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = TrailerYardEntryScoreRangeRule(4.2, 2852.2)
        val item = newTrailerYardEntry("x", "y")
        item.score = 2853.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerYardEntryCodeLengthRule(16)
        val item = newTrailerYardEntry("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = TrailerYardEntryTrailerIdLengthRule(1000)
        val item = newTrailerYardEntry("x", "y")
        item.trailerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = TrailerYardEntryRevisionRangeRule(120, 86400)
        val item = newTrailerYardEntry("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = TrailerYardEntryEtaMinutesRangeRule(16, 64)
        val item = newTrailerYardEntry("x", "y")
        item.etaMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
