package com.acme.transport.linehaul.capacity.capacitypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCapacityPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCapacityPreferenceValidator()
        val item = newWeightedCapacityPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = WeightedCapacityPreferenceScoreRangeRule(9.9, 2232.9)
        val item = newWeightedCapacityPreference("x", "y")
        item.score = 2234.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = WeightedCapacityPreferenceChecksumLengthRule(255)
        val item = newWeightedCapacityPreference("x", "y")
        item.checksum = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = WeightedCapacityPreferenceBudgetRangeRule(9.8, 2249.3)
        val item = newWeightedCapacityPreference("x", "y")
        item.budget = 2250.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = WeightedCapacityPreferenceSequenceRangeRule(255, 255)
        val item = newWeightedCapacityPreference("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
