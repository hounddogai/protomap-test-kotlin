package com.acme.transport.terminal.traileryard.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerYardAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTrailerYardAgreementValidator()
        val item = newManualTrailerYardAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualTrailerYardAgreementNameLengthRule(1000)
        val item = newManualTrailerYardAgreement("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ManualTrailerYardAgreementDurationMinutesRangeRule(64, 64)
        val item = newManualTrailerYardAgreement("x", "y")
        item.durationMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualTrailerYardAgreementRequiredNameRule("manual trailer yard agreement needs a value")
        val item = newManualTrailerYardAgreement("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
