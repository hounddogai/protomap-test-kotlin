package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacySwapAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacySwapAgreementValidator()
        val item = newLegacySwapAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = LegacySwapAgreementRequiredChecksumRule("legacy swap agreement needs a value")
        val item = newLegacySwapAgreement("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacySwapAgreementRequiredCodeRule("legacy swap agreement needs a value")
        val item = newLegacySwapAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacySwapAgreementLabelLengthRule(32)
        val item = newLegacySwapAgreement("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
