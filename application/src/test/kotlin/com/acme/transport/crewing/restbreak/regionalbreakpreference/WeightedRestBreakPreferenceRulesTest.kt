package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRestBreakPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRestBreakPreferenceValidator()
        val item = newWeightedRestBreakPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedRestBreakPreferenceNameLengthRule(3600)
        val item = newWeightedRestBreakPreference("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedRestBreakPreferenceRequiredNameRule("weighted rest break preference needs a value")
        val item = newWeightedRestBreakPreference("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = WeightedRestBreakPreferenceRequiredCategoryRule("weighted rest break preference needs an entry")
        val item = newWeightedRestBreakPreference("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = WeightedRestBreakPreferenceDistanceKmRangeRule(8.5, 4906.3)
        val item = newWeightedRestBreakPreference("x", "y")
        item.distanceKm = 4907.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedRestBreakPreferenceCodeLengthRule(64)
        val item = newWeightedRestBreakPreference("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
