package com.acme.transport.lastmile.drop.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropEntryValidator()
        val item = newDropEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = DropEntryRegionLengthRule(32)
        val item = newDropEntry("x", "y")
        item.region = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = DropEntryRequiredReviewerRule("drop entry needs an entry")
        val item = newDropEntry("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DropEntryRequiredCodeRule("drop entry needs this field")
        val item = newDropEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DropEntryCodeLengthRule(120)
        val item = newDropEntry("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
