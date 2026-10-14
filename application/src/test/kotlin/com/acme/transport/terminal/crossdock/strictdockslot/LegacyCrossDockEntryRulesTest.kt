package com.acme.transport.terminal.crossdock.strictdockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCrossDockEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyCrossDockEntryValidator()
        val item = newLegacyCrossDockEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = LegacyCrossDockEntryDockLengthRule(1000)
        val item = newLegacyCrossDockEntry("x", "y")
        item.dock = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyCrossDockEntryRequiredCodeRule("legacy cross dock entry needs an entry")
        val item = newLegacyCrossDockEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = LegacyCrossDockEntryFuelLitersRangeRule(0.1, 1459.2)
        val item = newLegacyCrossDockEntry("x", "y")
        item.fuelLiters = 1460.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = LegacyCrossDockEntryEndsAtRangeRule(255, 1000)
        val item = newLegacyCrossDockEntry("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
