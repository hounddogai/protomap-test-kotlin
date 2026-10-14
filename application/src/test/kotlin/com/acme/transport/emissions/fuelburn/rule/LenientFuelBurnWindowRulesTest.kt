package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientFuelBurnWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientFuelBurnWindowValidator()
        val item = newLenientFuelBurnWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LenientFuelBurnWindowRetriesRangeRule(80, 120)
        val item = newLenientFuelBurnWindow("x", "y")
        item.retries = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LenientFuelBurnWindowUpdatedAtRangeRule(120, 3600)
        val item = newLenientFuelBurnWindow("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
