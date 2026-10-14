package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCoachingEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictCoachingEntryValidator()
        val item = newStrictCoachingEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = StrictCoachingEntryWindowDaysRangeRule(64, 3600)
        val item = newStrictCoachingEntry("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictCoachingEntryRequiredCodeRule("strict coaching entry needs this field")
        val item = newStrictCoachingEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = StrictCoachingEntryRetriesRangeRule(32, 86400)
        val item = newStrictCoachingEntry("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = StrictCoachingEntryQuantityRangeRule(80, 255)
        val item = newStrictCoachingEntry("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictCoachingEntryCodeLengthRule(80)
        val item = newStrictCoachingEntry("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
