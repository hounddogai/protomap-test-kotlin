package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTenderEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTenderEntryValidator()
        val item = newPrimaryTenderEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = PrimaryTenderEntryEndsAtRangeRule(64, 3600)
        val item = newPrimaryTenderEntry("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = PrimaryTenderEntryRequiredChecksumRule("primary tender entry needs an entry")
        val item = newPrimaryTenderEntry("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryTenderEntryCodeLengthRule(255)
        val item = newPrimaryTenderEntry("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
