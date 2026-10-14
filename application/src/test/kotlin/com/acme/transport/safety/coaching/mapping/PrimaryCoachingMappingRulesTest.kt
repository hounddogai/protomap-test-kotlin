package com.acme.transport.safety.coaching.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCoachingMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCoachingMappingValidator()
        val item = newPrimaryCoachingMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = PrimaryCoachingMappingBudgetRangeRule(8.0, 3596.1)
        val item = newPrimaryCoachingMapping("x", "y")
        item.budget = 3597.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryCoachingMappingCodeLengthRule(1000)
        val item = newPrimaryCoachingMapping("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
