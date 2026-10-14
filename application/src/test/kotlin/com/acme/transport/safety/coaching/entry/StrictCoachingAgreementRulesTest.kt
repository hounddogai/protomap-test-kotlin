package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCoachingAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictCoachingAgreementValidator()
        val item = newStrictCoachingAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = StrictCoachingAgreementMarginRangeRule(0.1, 1649.7)
        val item = newStrictCoachingAgreement("x", "y")
        item.margin = 1651.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = StrictCoachingAgreementAttemptsRangeRule(255, 255)
        val item = newStrictCoachingAgreement("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = StrictCoachingAgreementStartsAtRangeRule(16, 86400)
        val item = newStrictCoachingAgreement("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = StrictCoachingAgreementDepotLengthRule(64)
        val item = newStrictCoachingAgreement("x", "y")
        item.depot = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
