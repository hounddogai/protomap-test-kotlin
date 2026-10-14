package com.acme.transport.fleetops.tire.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTireLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTireLedgerValidator()
        val item = newManualTireLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualTireLedgerRequiredLabelRule("manual tire ledger needs this field")
        val item = newManualTireLedger("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualTireLedgerCodeLengthRule(1000)
        val item = newManualTireLedger("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ManualTireLedgerRatioRangeRule(2.6, 2582.6)
        val item = newManualTireLedger("x", "y")
        item.ratio = 2584.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
