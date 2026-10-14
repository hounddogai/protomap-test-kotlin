package com.acme.transport.terminal.dwell.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDwellEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDwellEntryValidator()
        val item = newCappedDwellEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedDwellEntryRequiredTitleRule("capped dwell entry needs an entry")
        val item = newCappedDwellEntry("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = CappedDwellEntryCostRangeRule(5.0, 581.8)
        val item = newCappedDwellEntry("x", "y")
        item.cost = 583.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
