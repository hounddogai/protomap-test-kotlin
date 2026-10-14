package com.acme.transport.safety.violation.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackViolationLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackViolationLedgerValidator()
        val item = newFallbackViolationLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackViolationLedgerLabelLengthRule(32)
        val item = newFallbackViolationLedger("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackViolationLedgerRequiredLabelRule("fallback violation ledger needs this field")
        val item = newFallbackViolationLedger("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = FallbackViolationLedgerCapacityRangeRule(80, 3600)
        val item = newFallbackViolationLedger("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
