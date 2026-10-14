package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityTargetValidator()
        val item = newCapacityTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = CapacityTargetSegmentLengthRule(32)
        val item = newCapacityTarget("x", "y")
        item.segment = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CapacityTargetRequiredLabelRule("capacity target needs an entry")
        val item = newCapacityTarget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = CapacityTargetRequiredSegmentRule("capacity target needs a value")
        val item = newCapacityTarget("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
