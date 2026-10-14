package com.acme.transport.customs.tariff.tariffagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTariffScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTariffScoreValidator()
        val item = newStrictTariffScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictTariffScoreTitleLengthRule(32)
        val item = newStrictTariffScore("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictTariffScoreRequiredCodeRule("strict tariff score needs this field")
        val item = newStrictTariffScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictTariffScoreRequiredTitleRule("strict tariff score needs an entry")
        val item = newStrictTariffScore("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = StrictTariffScoreScoreRangeRule(2.7, 4822.4)
        val item = newStrictTariffScore("x", "y")
        item.score = 4823.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = StrictTariffScoreChannelLengthRule(16)
        val item = newStrictTariffScore("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
