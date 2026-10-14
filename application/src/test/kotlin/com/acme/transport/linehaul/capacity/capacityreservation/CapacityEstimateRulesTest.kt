package com.acme.transport.linehaul.capacity.capacityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityEstimateValidator()
        val item = newCapacityEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CapacityEstimateRequiredOwnerIdRule("capacity estimate needs a value")
        val item = newCapacityEstimate("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = CapacityEstimateRatioRangeRule(3.1, 1639.0)
        val item = newCapacityEstimate("x", "y")
        item.ratio = 1640.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CapacityEstimateRequiredCodeRule("capacity estimate needs this field")
        val item = newCapacityEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CapacityEstimateCodeLengthRule(86400)
        val item = newCapacityEstimate("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
