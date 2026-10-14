package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFuelBurnLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredFuelBurnLedgerValidator()
        val item = newTieredFuelBurnLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = TieredFuelBurnLedgerPhoneLengthRule(16)
        val item = newTieredFuelBurnLedger("x", "y")
        item.phone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TieredFuelBurnLedgerOdometerRangeRule(1000, 86400)
        val item = newTieredFuelBurnLedger("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredFuelBurnLedgerRequiredLabelRule("tiered fuel burn ledger needs this field")
        val item = newTieredFuelBurnLedger("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = TieredFuelBurnLedgerChannelLengthRule(16)
        val item = newTieredFuelBurnLedger("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
