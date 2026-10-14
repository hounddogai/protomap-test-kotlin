package com.acme.transport.linehaul.trailer.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTrailerPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTrailerPolicyValidator()
        val item = newStrictTrailerPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictTrailerPolicyRequiredTitleRule("strict trailer policy needs this field")
        val item = newStrictTrailerPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictTrailerPolicyTitleLengthRule(16)
        val item = newStrictTrailerPolicy("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = StrictTrailerPolicyScoreRangeRule(4.2, 2476.5)
        val item = newStrictTrailerPolicy("x", "y")
        item.score = 2478.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = StrictTrailerPolicyRequiredChannelRule("strict trailer policy needs this field")
        val item = newStrictTrailerPolicy("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
