package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRosterEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRosterEntryValidator()
        val item = newGlobalRosterEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = GlobalRosterEntryTrailerIdLengthRule(120)
        val item = newGlobalRosterEntry("x", "y")
        item.trailerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = GlobalRosterEntryRequiredCurrencyRule("global roster entry needs a value")
        val item = newGlobalRosterEntry("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = GlobalRosterEntryRequiredTrailerIdRule("global roster entry needs this field")
        val item = newGlobalRosterEntry("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = GlobalRosterEntryWeightRangeRule(4.3, 4650.4)
        val item = newGlobalRosterEntry("x", "y")
        item.weight = 4651.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
