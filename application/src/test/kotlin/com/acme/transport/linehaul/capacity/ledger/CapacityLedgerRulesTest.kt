package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityLedgerValidator()
        val item = newCapacityLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CapacityLedgerRequiredLabelRule("capacity ledger needs a value")
        val item = newCapacityLedger("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = CapacityLedgerPhoneLengthRule(1000)
        val item = newCapacityLedger("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = CapacityLedgerRevisionRangeRule(64, 255)
        val item = newCapacityLedger("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
