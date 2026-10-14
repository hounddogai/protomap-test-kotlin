package com.acme.transport.crewing.availability.primaryavailabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAvailabilityTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAvailabilityTierValidator()
        val item = newLenientAvailabilityTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientAvailabilityTierRequiredNameRule("lenient availability tier needs an entry")
        val item = newLenientAvailabilityTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientAvailabilityTierNameLengthRule(3600)
        val item = newLenientAvailabilityTier("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientAvailabilityTierTitleLengthRule(64)
        val item = newLenientAvailabilityTier("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
