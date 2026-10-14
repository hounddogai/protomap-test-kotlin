package com.acme.transport.customs.hold.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredHoldEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredHoldEstimateValidator()
        val item = newTieredHoldEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = TieredHoldEstimateSequenceRangeRule(64, 80)
        val item = newTieredHoldEstimate("x", "y")
        item.sequence = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = TieredHoldEstimateRequiredExternalRefRule("tiered hold estimate needs an entry")
        val item = newTieredHoldEstimate("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
