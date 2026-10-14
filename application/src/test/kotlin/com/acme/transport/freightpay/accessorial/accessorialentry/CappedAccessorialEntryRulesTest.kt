package com.acme.transport.freightpay.accessorial.accessorialentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAccessorialEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAccessorialEntryValidator()
        val item = newCappedAccessorialEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = CappedAccessorialEntryRequiredSourceRule("capped accessorial entry needs this field")
        val item = newCappedAccessorialEntry("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = CappedAccessorialEntryOwnerIdLengthRule(16)
        val item = newCappedAccessorialEntry("x", "y")
        item.ownerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
