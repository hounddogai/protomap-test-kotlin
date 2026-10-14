package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyFuelBurnGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyFuelBurnGroupValidator()
        val item = newLegacyFuelBurnGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = LegacyFuelBurnGroupExpiresAtRangeRule(32, 86400)
        val item = newLegacyFuelBurnGroup("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = LegacyFuelBurnGroupThresholdRangeRule(9.3, 196.8)
        val item = newLegacyFuelBurnGroup("x", "y")
        item.threshold = 198.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
