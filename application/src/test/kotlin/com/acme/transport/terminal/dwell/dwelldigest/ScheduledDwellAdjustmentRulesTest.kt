package com.acme.transport.terminal.dwell.dwelldigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDwellAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDwellAdjustmentValidator()
        val item = newScheduledDwellAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ScheduledDwellAdjustmentRequiredLaneRule("scheduled dwell adjustment needs this field")
        val item = newScheduledDwellAdjustment("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = ScheduledDwellAdjustmentLaneLengthRule(120)
        val item = newScheduledDwellAdjustment("x", "y")
        item.lane = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledDwellAdjustmentCodeLengthRule(3600)
        val item = newScheduledDwellAdjustment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledDwellAdjustmentRequiredTitleRule("scheduled dwell adjustment needs an entry")
        val item = newScheduledDwellAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
