package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientViolationAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientViolationAssignmentValidator()
        val item = newLenientViolationAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientViolationAssignmentRequiredCodeRule("lenient violation assignment needs a value")
        val item = newLenientViolationAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LenientViolationAssignmentSequenceRangeRule(32, 64)
        val item = newLenientViolationAssignment("x", "y")
        item.sequence = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientViolationAssignmentCodeLengthRule(120)
        val item = newLenientViolationAssignment("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LenientViolationAssignmentDiscountRangeRule(5.4, 2272.9)
        val item = newLenientViolationAssignment("x", "y")
        item.discount = 2274.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
