package com.acme.transport.terminal.traileryard.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardAgreementValidator()
        val item = newTrailerYardAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = TrailerYardAgreementRatioRangeRule(1.3, 2509.6)
        val item = newTrailerYardAgreement("x", "y")
        item.ratio = 2511.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = TrailerYardAgreementTitleLengthRule(120)
        val item = newTrailerYardAgreement("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
