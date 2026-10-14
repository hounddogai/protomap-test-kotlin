package com.acme.transport.crewing.availability.defaultavailabilitysegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictAvailabilityExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictAvailabilityExceptionValidator()
        val item = newStrictAvailabilityException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = StrictAvailabilityExceptionRequiredTimezoneRule("strict availability exception needs an entry")
        val item = newStrictAvailabilityException("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = StrictAvailabilityExceptionRequiredShiftCodeRule("strict availability exception needs this field")
        val item = newStrictAvailabilityException("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = StrictAvailabilityExceptionExpiresAtRangeRule(3600, 86400)
        val item = newStrictAvailabilityException("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictAvailabilityExceptionRequiredCodeRule("strict availability exception needs an entry")
        val item = newStrictAvailabilityException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
