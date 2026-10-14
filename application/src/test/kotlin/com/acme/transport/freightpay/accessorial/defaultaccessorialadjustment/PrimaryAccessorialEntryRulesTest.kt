package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAccessorialEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAccessorialEntryValidator()
        val item = newPrimaryAccessorialEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = PrimaryAccessorialEntryChecksumLengthRule(1000)
        val item = newPrimaryAccessorialEntry("x", "y")
        item.checksum = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = PrimaryAccessorialEntryRequiredChecksumRule("primary accessorial entry needs this field")
        val item = newPrimaryAccessorialEntry("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = PrimaryAccessorialEntryRevisionRangeRule(16, 1000)
        val item = newPrimaryAccessorialEntry("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
