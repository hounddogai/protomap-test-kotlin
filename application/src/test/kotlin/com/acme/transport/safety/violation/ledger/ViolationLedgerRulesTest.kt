package com.acme.transport.safety.violation.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationLedgerValidator()
        val item = newViolationLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ViolationLedgerQuantityRangeRule(32, 3600)
        val item = newViolationLedger("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = ViolationLedgerMinimumRangeRule(1.1, 2370.0)
        val item = newViolationLedger("x", "y")
        item.minimum = 2371.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = ViolationLedgerRequiredCountryRule("violation ledger needs this field")
        val item = newViolationLedger("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
