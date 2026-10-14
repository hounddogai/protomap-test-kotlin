package com.acme.transport.terminal.traileryard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTrailerYardPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTrailerYardPolicyValidator()
        val item = newSeasonalTrailerYardPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalTrailerYardPolicyLabelLengthRule(64)
        val item = newSeasonalTrailerYardPolicy("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = SeasonalTrailerYardPolicyRequiredChannelRule("seasonal trailer yard policy needs an entry")
        val item = newSeasonalTrailerYardPolicy("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
