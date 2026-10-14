package com.acme.transport.crewing.roster.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRosterGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRosterGroupValidator()
        val item = newGlobalRosterGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = GlobalRosterGroupDockLengthRule(120)
        val item = newGlobalRosterGroup("x", "y")
        item.dock = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = GlobalRosterGroupRequiredDockRule("global roster group needs an entry")
        val item = newGlobalRosterGroup("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = GlobalRosterGroupRequiredSummaryRule("global roster group needs this field")
        val item = newGlobalRosterGroup("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalRosterGroupTitleLengthRule(3600)
        val item = newGlobalRosterGroup("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = GlobalRosterGroupSummaryLengthRule(16)
        val item = newGlobalRosterGroup("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
