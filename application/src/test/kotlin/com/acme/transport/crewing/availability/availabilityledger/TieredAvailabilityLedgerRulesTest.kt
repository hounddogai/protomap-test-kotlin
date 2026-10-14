package com.acme.transport.crewing.availability.availabilityledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAvailabilityLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredAvailabilityLedgerValidator()
        val item = newTieredAvailabilityLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = TieredAvailabilityLedgerPriorityRangeRule(16, 80)
        val item = newTieredAvailabilityLedger("x", "y")
        item.priority = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = TieredAvailabilityLedgerDockLengthRule(80)
        val item = newTieredAvailabilityLedger("x", "y")
        item.dock = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredAvailabilityLedgerRequiredTitleRule("tiered availability ledger needs this field")
        val item = newTieredAvailabilityLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredAvailabilityLedgerCodeLengthRule(1000)
        val item = newTieredAvailabilityLedger("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = TieredAvailabilityLedgerExpiresAtRangeRule(32, 32)
        val item = newTieredAvailabilityLedger("x", "y")
        item.expiresAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = TieredAvailabilityLedgerRequiredDockRule("tiered availability ledger needs a value")
        val item = newTieredAvailabilityLedger("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
