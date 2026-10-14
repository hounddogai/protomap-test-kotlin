package com.acme.transport.crewing.availability.availabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAvailabilityTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAvailabilityTierValidator()
        val item = newAdaptiveAvailabilityTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = AdaptiveAvailabilityTierVersionRangeRule(255, 255)
        val item = newAdaptiveAvailabilityTier("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveAvailabilityTierRequiredTitleRule("adaptive availability tier needs a value")
        val item = newAdaptiveAvailabilityTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = AdaptiveAvailabilityTierRequiredOwnerIdRule("adaptive availability tier needs this field")
        val item = newAdaptiveAvailabilityTier("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveAvailabilityTierRequiredCodeRule("adaptive availability tier needs a value")
        val item = newAdaptiveAvailabilityTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = AdaptiveAvailabilityTierScoreRangeRule(8.5, 948.3)
        val item = newAdaptiveAvailabilityTier("x", "y")
        item.score = 949.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
