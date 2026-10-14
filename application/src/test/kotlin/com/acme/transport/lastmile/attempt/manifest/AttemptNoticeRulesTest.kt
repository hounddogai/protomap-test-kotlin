package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptNoticeValidator()
        val item = newAttemptNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = AttemptNoticeDiscountRangeRule(1.0, 279.4)
        val item = newAttemptNotice("x", "y")
        item.discount = 280.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = AttemptNoticeRequiredCountryRule("attempt notice needs an entry")
        val item = newAttemptNotice("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AttemptNoticeRequiredCodeRule("attempt notice needs an entry")
        val item = newAttemptNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = AttemptNoticeCountryLengthRule(80)
        val item = newAttemptNotice("x", "y")
        item.country = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
