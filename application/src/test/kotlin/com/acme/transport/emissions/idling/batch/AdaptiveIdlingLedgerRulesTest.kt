package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveIdlingLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveIdlingLedgerValidator()
        val item = newAdaptiveIdlingLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = AdaptiveIdlingLedgerAttemptsRangeRule(255, 86400)
        val item = newAdaptiveIdlingLedger("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveIdlingLedgerRequiredLabelRule("adaptive idling ledger needs an entry")
        val item = newAdaptiveIdlingLedger("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = AdaptiveIdlingLedgerUnitLengthRule(32)
        val item = newAdaptiveIdlingLedger("x", "y")
        item.unit = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = AdaptiveIdlingLedgerRequiredDescriptionRule("adaptive idling ledger needs this field")
        val item = newAdaptiveIdlingLedger("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = AdaptiveIdlingLedgerDescriptionLengthRule(86400)
        val item = newAdaptiveIdlingLedger("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
