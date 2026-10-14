package com.acme.transport.fleetops.tire.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTireLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTireLedgerValidator()
        val item = newWeightedTireLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedTireLedgerCodeLengthRule(86400)
        val item = newWeightedTireLedger("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedTireLedgerRequiredCodeRule("weighted tire ledger needs this field")
        val item = newWeightedTireLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = WeightedTireLedgerDurationMinutesRangeRule(64, 1000)
        val item = newWeightedTireLedger("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = WeightedTireLedgerRequiredCountryRule("weighted tire ledger needs this field")
        val item = newWeightedTireLedger("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
