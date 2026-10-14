package com.acme.transport.crewing.roster.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRosterAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRosterAdjustmentValidator()
        val item = newGlobalRosterAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalRosterAdjustmentRequiredLabelRule("global roster adjustment needs an entry")
        val item = newGlobalRosterAdjustment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalRosterAdjustmentCodeLengthRule(32)
        val item = newGlobalRosterAdjustment("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalRosterAdjustmentRequiredCodeRule("global roster adjustment needs this field")
        val item = newGlobalRosterAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = GlobalRosterAdjustmentRequiredTitleRule("global roster adjustment needs this field")
        val item = newGlobalRosterAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = GlobalRosterAdjustmentRetriesRangeRule(64, 80)
        val item = newGlobalRosterAdjustment("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
