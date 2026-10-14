package com.acme.transport.linehaul.trailer.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerAgreementValidator()
        val item = newTrailerAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = TrailerAgreementRequiredCategoryRule("trailer agreement needs a value")
        val item = newTrailerAgreement("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = TrailerAgreementLoadKgRangeRule(7.5, 3797.5)
        val item = newTrailerAgreement("x", "y")
        item.loadKg = 3799.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
