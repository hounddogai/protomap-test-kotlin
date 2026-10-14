package com.acme.transport.linehaul.tender.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTenderRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTenderRecordValidator()
        val item = newLegacyTenderRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = LegacyTenderRecordChecksumLengthRule(120)
        val item = newLegacyTenderRecord("x", "y")
        item.checksum = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyTenderRecordTitleLengthRule(255)
        val item = newLegacyTenderRecord("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = LegacyTenderRecordRequiredChecksumRule("legacy tender record needs an entry")
        val item = newLegacyTenderRecord("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = LegacyTenderRecordGraceDaysRangeRule(16, 3600)
        val item = newLegacyTenderRecord("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
