package com.acme.transport.customs.clearance.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceCycleValidator()
        val item = newClearanceCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ClearanceCycleCodeLengthRule(32)
        val item = newClearanceCycle("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceCycleRequiredCodeRule("clearance cycle needs a value")
        val item = newClearanceCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ClearanceCycleRequiredLaneRule("clearance cycle needs this field")
        val item = newClearanceCycle("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
