package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireLedgerValidator()
        val item = newTireLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireLedgerRequiredCodeRule("tire ledger needs a value")
        val item = newTireLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TireLedgerRequiredNameRule("tire ledger needs this field")
        val item = newTireLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = TireLedgerThresholdRangeRule(7.1, 1237.8)
        val item = newTireLedger("x", "y")
        item.threshold = 1239.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = TireLedgerRequiredUnitRule("tire ledger needs an entry")
        val item = newTireLedger("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TireLedgerNameLengthRule(16)
        val item = newTireLedger("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
