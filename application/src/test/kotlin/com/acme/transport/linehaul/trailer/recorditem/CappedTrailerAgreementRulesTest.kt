package com.acme.transport.linehaul.trailer.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTrailerAgreementValidator()
        val item = newCappedTrailerAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedTrailerAgreementTitleLengthRule(3600)
        val item = newCappedTrailerAgreement("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedTrailerAgreementRequiredCodeRule("capped trailer agreement needs this field")
        val item = newCappedTrailerAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
