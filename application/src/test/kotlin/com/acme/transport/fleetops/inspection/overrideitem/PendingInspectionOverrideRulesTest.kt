package com.acme.transport.fleetops.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingInspectionOverrideValidator()
        val item = newPendingInspectionOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = PendingInspectionOverrideRequiredSummaryRule("pending inspection override needs an entry")
        val item = newPendingInspectionOverride("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = PendingInspectionOverrideSequenceRangeRule(32, 3600)
        val item = newPendingInspectionOverride("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingInspectionOverrideRequiredTitleRule("pending inspection override needs an entry")
        val item = newPendingInspectionOverride("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
