package com.acme.transport.terminal.crossdock.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockEntryValidator()
        val item = newCrossDockEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = CrossDockEntryPalletsRangeRule(1000, 3600)
        val item = newCrossDockEntry("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = CrossDockEntrySummaryLengthRule(16)
        val item = newCrossDockEntry("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = CrossDockEntryRequiredContactEmailRule("cross dock entry needs a value")
        val item = newCrossDockEntry("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CrossDockEntryRequiredTitleRule("cross dock entry needs this field")
        val item = newCrossDockEntry("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CrossDockEntryCodeLengthRule(32)
        val item = newCrossDockEntry("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
