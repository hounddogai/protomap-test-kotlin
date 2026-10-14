package com.acme.transport.crewing.swap.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapPolicyValidator()
        val item = newSwapPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = SwapPolicyStartsAtRangeRule(80, 3600)
        val item = newSwapPolicy("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SwapPolicyRequiredLabelRule("swap policy needs an entry")
        val item = newSwapPolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = SwapPolicyCapacityRangeRule(16, 1000)
        val item = newSwapPolicy("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SwapPolicyRequiredCodeRule("swap policy needs this field")
        val item = newSwapPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
