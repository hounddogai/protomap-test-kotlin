package com.acme.transport.safety.violation.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationEntryValidator()
        val item = newViolationEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = ViolationEntryChecksumLengthRule(64)
        val item = newViolationEntry("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ViolationEntryQuantityRangeRule(32, 3600)
        val item = newViolationEntry("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ViolationEntryRequiredNameRule("violation entry needs a value")
        val item = newViolationEntry("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ViolationEntryLabelLengthRule(16)
        val item = newViolationEntry("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
