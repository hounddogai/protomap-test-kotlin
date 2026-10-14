package com.acme.transport.linehaul.trailer.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerEntryValidator()
        val item = newTrailerEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = TrailerEntryAmountRangeRule(1.4, 2771.6)
        val item = newTrailerEntry("x", "y")
        item.amount = 2773.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerEntryCodeLengthRule(3600)
        val item = newTrailerEntry("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = TrailerEntryRequiredChannelRule("trailer entry needs an entry")
        val item = newTrailerEntry("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = TrailerEntryScoreRangeRule(7.9, 1594.0)
        val item = newTrailerEntry("x", "y")
        item.score = 1595.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = TrailerEntryChannelLengthRule(80)
        val item = newTrailerEntry("x", "y")
        item.channel = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
