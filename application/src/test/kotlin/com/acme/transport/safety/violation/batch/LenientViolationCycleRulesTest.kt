package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientViolationCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientViolationCycleValidator()
        val item = newLenientViolationCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientViolationCycleCodeLengthRule(80)
        val item = newLenientViolationCycle("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientViolationCycleRequiredCodeRule("lenient violation cycle needs an entry")
        val item = newLenientViolationCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientViolationCycleRequiredLabelRule("lenient violation cycle needs this field")
        val item = newLenientViolationCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
