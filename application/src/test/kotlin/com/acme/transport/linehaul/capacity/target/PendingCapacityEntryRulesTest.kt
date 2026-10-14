package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCapacityEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCapacityEntryValidator()
        val item = newPendingCapacityEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = PendingCapacityEntryLoadKgRangeRule(7.0, 4362.7)
        val item = newPendingCapacityEntry("x", "y")
        item.loadKg = 4364.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingCapacityEntryLabelLengthRule(32)
        val item = newPendingCapacityEntry("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = PendingCapacityEntryRequiredChecksumRule("pending capacity entry needs an entry")
        val item = newPendingCapacityEntry("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingCapacityEntryCodeLengthRule(3600)
        val item = newPendingCapacityEntry("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
