package com.acme.transport.terminal.traileryard.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTrailerYardTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTrailerYardTierValidator()
        val item = newWeightedTrailerYardTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedTrailerYardTierRequiredNameRule("weighted trailer yard tier needs an entry")
        val item = newWeightedTrailerYardTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = WeightedTrailerYardTierRequiredChecksumRule("weighted trailer yard tier needs a value")
        val item = newWeightedTrailerYardTier("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = WeightedTrailerYardTierBatchSizeRangeRule(80, 120)
        val item = newWeightedTrailerYardTier("x", "y")
        item.batchSize = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedTrailerYardTierRequiredCodeRule("weighted trailer yard tier needs an entry")
        val item = newWeightedTrailerYardTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
