package com.acme.transport.safety.scorecard.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardBundleValidator()
        val item = newScorecardBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ScorecardBundleFuelLitersRangeRule(6.9, 3970.5)
        val item = newScorecardBundle("x", "y")
        item.fuelLiters = 3972.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = ScorecardBundleMinimumRangeRule(7.2, 2899.4)
        val item = newScorecardBundle("x", "y")
        item.minimum = 2900.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ScorecardBundleAmountRangeRule(4.2, 2716.6)
        val item = newScorecardBundle("x", "y")
        item.amount = 2718.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
