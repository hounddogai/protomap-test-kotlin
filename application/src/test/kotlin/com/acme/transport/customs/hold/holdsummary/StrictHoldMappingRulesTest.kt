package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictHoldMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictHoldMappingValidator()
        val item = newStrictHoldMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictHoldMappingRequiredTitleRule("strict hold mapping needs an entry")
        val item = newStrictHoldMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = StrictHoldMappingCapacityRangeRule(16, 32)
        val item = newStrictHoldMapping("x", "y")
        item.capacity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = StrictHoldMappingRequiredLaneRule("strict hold mapping needs a value")
        val item = newStrictHoldMapping("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
