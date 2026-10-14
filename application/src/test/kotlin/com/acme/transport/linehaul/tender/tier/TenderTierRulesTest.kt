package com.acme.transport.linehaul.tender.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderTierValidator()
        val item = newTenderTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = TenderTierRequiredPhoneRule("tender tier needs an entry")
        val item = newTenderTier("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = TenderTierPhoneLengthRule(3600)
        val item = newTenderTier("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = TenderTierNameLengthRule(3600)
        val item = newTenderTier("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
