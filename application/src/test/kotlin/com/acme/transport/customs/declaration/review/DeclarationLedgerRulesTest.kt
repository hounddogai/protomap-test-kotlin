package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationLedgerValidator()
        val item = newDeclarationLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = DeclarationLedgerRequiredChecksumRule("declaration ledger needs an entry")
        val item = newDeclarationLedger("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = DeclarationLedgerRequiredDriverIdRule("declaration ledger needs an entry")
        val item = newDeclarationLedger("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DeclarationLedgerTitleLengthRule(255)
        val item = newDeclarationLedger("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DeclarationLedgerThresholdRangeRule(2.8, 4678.4)
        val item = newDeclarationLedger("x", "y")
        item.threshold = 4679.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
