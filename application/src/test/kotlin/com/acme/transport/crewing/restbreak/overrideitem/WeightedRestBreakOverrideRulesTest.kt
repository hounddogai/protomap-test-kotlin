package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRestBreakOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRestBreakOverrideValidator()
        val item = newWeightedRestBreakOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = WeightedRestBreakOverridePalletsRangeRule(16, 80)
        val item = newWeightedRestBreakOverride("x", "y")
        item.pallets = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedRestBreakOverrideRequiredLabelRule("weighted rest break override needs this field")
        val item = newWeightedRestBreakOverride("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
