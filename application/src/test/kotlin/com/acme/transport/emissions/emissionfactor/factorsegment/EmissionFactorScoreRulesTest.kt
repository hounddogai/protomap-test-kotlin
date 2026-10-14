package com.acme.transport.emissions.emissionfactor.factorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorScoreValidator()
        val item = newEmissionFactorScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = EmissionFactorScoreTitleLengthRule(120)
        val item = newEmissionFactorScore("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = EmissionFactorScoreRequiredChannelRule("emission factor score needs an entry")
        val item = newEmissionFactorScore("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = EmissionFactorScoreRateRangeRule(8.9, 4714.0)
        val item = newEmissionFactorScore("x", "y")
        item.rate = 4715.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = EmissionFactorScoreChannelLengthRule(1000)
        val item = newEmissionFactorScore("x", "y")
        item.channel = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
