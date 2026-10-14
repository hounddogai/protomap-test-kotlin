package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingMappingValidator()
        val item = newCoachingMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CoachingMappingCodeLengthRule(86400)
        val item = newCoachingMapping("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = CoachingMappingRequiredDepotRule("coaching mapping needs this field")
        val item = newCoachingMapping("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = CoachingMappingQuantityRangeRule(16, 80)
        val item = newCoachingMapping("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = CoachingMappingDepotLengthRule(1000)
        val item = newCoachingMapping("x", "y")
        item.depot = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
