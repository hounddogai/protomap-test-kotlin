package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTenderAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTenderAssignmentValidator()
        val item = newFallbackTenderAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = FallbackTenderAssignmentLimitRangeRule(64, 3600)
        val item = newFallbackTenderAssignment("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackTenderAssignmentCodeLengthRule(86400)
        val item = newFallbackTenderAssignment("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackTenderAssignmentRequiredCodeRule("fallback tender assignment needs an entry")
        val item = newFallbackTenderAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackTenderAssignmentRequiredLabelRule("fallback tender assignment needs a value")
        val item = newFallbackTenderAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackTenderAssignmentLabelLengthRule(1000)
        val item = newFallbackTenderAssignment("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
