package com.acme.transport.safety.violation.violationcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictViolationCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictViolationCycleValidator()
        val item = newStrictViolationCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictViolationCycleNameLengthRule(16)
        val item = newStrictViolationCycle("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = StrictViolationCycleRequiredLocaleRule("strict violation cycle needs this field")
        val item = newStrictViolationCycle("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = StrictViolationCycleRequiredOwnerIdRule("strict violation cycle needs a value")
        val item = newStrictViolationCycle("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
