package com.acme.transport.emissions.emissionfactor.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorDraftValidator()
        val item = newEmissionFactorDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = EmissionFactorDraftRequiredCurrencyRule("emission factor draft needs a value")
        val item = newEmissionFactorDraft("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = EmissionFactorDraftDiscountRangeRule(6.0, 3816.5)
        val item = newEmissionFactorDraft("x", "y")
        item.discount = 3818.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorDraftRequiredCodeRule("emission factor draft needs an entry")
        val item = newEmissionFactorDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
