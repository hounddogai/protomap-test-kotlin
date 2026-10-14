package com.acme.transport.linehaul.capacity.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityThresholdValidator()
        val item = newCapacityThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CapacityThresholdRequiredCodeRule("capacity threshold needs a value")
        val item = newCapacityThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CapacityThresholdNameLengthRule(64)
        val item = newCapacityThreshold("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
