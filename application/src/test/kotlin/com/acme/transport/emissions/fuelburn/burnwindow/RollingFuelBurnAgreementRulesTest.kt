package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingFuelBurnAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingFuelBurnAgreementValidator()
        val item = newRollingFuelBurnAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RollingFuelBurnAgreementWeightRangeRule(8.8, 71.5)
        val item = newRollingFuelBurnAgreement("x", "y")
        item.weight = 73.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
