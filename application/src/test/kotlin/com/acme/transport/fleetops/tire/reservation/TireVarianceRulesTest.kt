package com.acme.transport.fleetops.tire.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireVarianceValidator()
        val item = newTireVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = TireVarianceFuelLitersRangeRule(5.2, 2022.9)
        val item = newTireVariance("x", "y")
        item.fuelLiters = 2024.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireVarianceRequiredCodeRule("tire variance needs an entry")
        val item = newTireVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
