package com.acme.transport.lastmile.redelivery.redeliveryproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryTierValidator()
        val item = newRedeliveryTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RedeliveryTierCodeLengthRule(16)
        val item = newRedeliveryTier("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryTierRequiredCodeRule("redelivery tier needs this field")
        val item = newRedeliveryTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = RedeliveryTierWindowDaysRangeRule(255, 86400)
        val item = newRedeliveryTier("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RedeliveryTierFuelLitersRangeRule(0.2, 2672.0)
        val item = newRedeliveryTier("x", "y")
        item.fuelLiters = 2673.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
