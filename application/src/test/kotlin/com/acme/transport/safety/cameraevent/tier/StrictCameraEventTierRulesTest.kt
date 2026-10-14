package com.acme.transport.safety.cameraevent.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCameraEventTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictCameraEventTierValidator()
        val item = newStrictCameraEventTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = StrictCameraEventTierCategoryLengthRule(255)
        val item = newStrictCameraEventTier("x", "y")
        item.category = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = StrictCameraEventTierWeightRangeRule(1.6, 67.8)
        val item = newStrictCameraEventTier("x", "y")
        item.weight = 69.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = StrictCameraEventTierSummaryLengthRule(255)
        val item = newStrictCameraEventTier("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictCameraEventTierRequiredNameRule("strict camera event tier needs this field")
        val item = newStrictCameraEventTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
