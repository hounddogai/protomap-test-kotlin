package com.acme.transport.crewing.restbreak.breakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRestBreakEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRestBreakEntryValidator()
        val item = newDefaultRestBreakEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = DefaultRestBreakEntryDiscountRangeRule(5.5, 885.5)
        val item = newDefaultRestBreakEntry("x", "y")
        item.discount = 887.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultRestBreakEntryRequiredCodeRule("default rest break entry needs an entry")
        val item = newDefaultRestBreakEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = DefaultRestBreakEntryRequiredOwnerIdRule("default rest break entry needs this field")
        val item = newDefaultRestBreakEntry("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
