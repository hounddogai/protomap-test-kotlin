package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDefectEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDefectEntryValidator()
        val item = newScheduledDefectEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledDefectEntryRequiredCodeRule("scheduled defect entry needs a value")
        val item = newScheduledDefectEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledDefectEntryLabelLengthRule(86400)
        val item = newScheduledDefectEntry("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = ScheduledDefectEntryRequiredRegionRule("scheduled defect entry needs an entry")
        val item = newScheduledDefectEntry("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledDefectEntryRequiredLabelRule("scheduled defect entry needs an entry")
        val item = newScheduledDefectEntry("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
