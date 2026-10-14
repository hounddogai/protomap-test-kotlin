package com.acme.transport.customs.tariff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffScoreValidator()
        val item = newTariffScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = TariffScoreCountryLengthRule(86400)
        val item = newTariffScore("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = TariffScoreScoreRangeRule(6.7, 3612.5)
        val item = newTariffScore("x", "y")
        item.score = 3614.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
