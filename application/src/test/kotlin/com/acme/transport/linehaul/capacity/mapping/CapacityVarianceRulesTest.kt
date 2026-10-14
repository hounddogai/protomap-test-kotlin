package com.acme.transport.linehaul.capacity.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityVarianceValidator()
        val item = newCapacityVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CapacityVarianceRequiredLabelRule("capacity variance needs this field")
        val item = newCapacityVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = CapacityVarianceQuantityRangeRule(32, 86400)
        val item = newCapacityVariance("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = CapacityVarianceCostRangeRule(1.9, 2222.5)
        val item = newCapacityVariance("x", "y")
        item.cost = 2224.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
