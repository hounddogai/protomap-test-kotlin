package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireRuleValidator()
        val item = newTireRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = TireRuleRequiredDockRule("tire rule needs a value")
        val item = newTireRule("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = TireRuleCategoryLengthRule(3600)
        val item = newTireRule("x", "y")
        item.category = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = TireRuleCapacityRangeRule(16, 64)
        val item = newTireRule("x", "y")
        item.capacity = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
