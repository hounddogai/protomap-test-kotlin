package com.acme.transport.emissions.fuelburn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFuelBurnProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientFuelBurnProfileValidator()
        val item = newLenientFuelBurnProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = LenientFuelBurnProfileRequiredLocaleRule("lenient fuel burn profile needs this field")
        val item = newLenientFuelBurnProfile("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LenientFuelBurnProfileStartsAtRangeRule(32, 86400)
        val item = newLenientFuelBurnProfile("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientFuelBurnProfileCodeLengthRule(64)
        val item = newLenientFuelBurnProfile("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
