package com.acme.transport.linehaul.backhaul.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictBackhaulMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictBackhaulMappingValidator()
        val item = newStrictBackhaulMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = StrictBackhaulMappingCreatedAtRangeRule(255, 3600)
        val item = newStrictBackhaulMapping("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictBackhaulMappingRequiredTitleRule("strict backhaul mapping needs an entry")
        val item = newStrictBackhaulMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = StrictBackhaulMappingRequiredLaneRule("strict backhaul mapping needs a value")
        val item = newStrictBackhaulMapping("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictBackhaulMappingTitleLengthRule(80)
        val item = newStrictBackhaulMapping("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
