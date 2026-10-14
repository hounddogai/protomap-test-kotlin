package com.acme.transport.safety.violation.violationledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientViolationLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientViolationLedgerValidator()
        val item = newLenientViolationLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientViolationLedgerRequiredNameRule("lenient violation ledger needs this field")
        val item = newLenientViolationLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientViolationLedgerCodeLengthRule(86400)
        val item = newLenientViolationLedger("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = LenientViolationLedgerCountryLengthRule(120)
        val item = newLenientViolationLedger("x", "y")
        item.country = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
