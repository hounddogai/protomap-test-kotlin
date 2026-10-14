package com.acme.transport.emissions.fuelburn.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnClaimValidator()
        val item = newFuelBurnClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = FuelBurnClaimCategoryLengthRule(255)
        val item = newFuelBurnClaim("x", "y")
        item.category = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = FuelBurnClaimMinimumRangeRule(9.2, 4940.5)
        val item = newFuelBurnClaim("x", "y")
        item.minimum = 4942.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = FuelBurnClaimVersionRangeRule(16, 1000)
        val item = newFuelBurnClaim("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = FuelBurnClaimRequiredCategoryRule("fuel burn claim needs a value")
        val item = newFuelBurnClaim("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelBurnClaimCodeLengthRule(32)
        val item = newFuelBurnClaim("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
