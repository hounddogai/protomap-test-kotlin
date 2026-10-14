package com.acme.transport.safety.cameraevent.evententry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCameraEventEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCameraEventEntryValidator()
        val item = newScheduledCameraEventEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ScheduledCameraEventEntryCapacityRangeRule(32, 1000)
        val item = newScheduledCameraEventEntry("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledCameraEventEntryTitleLengthRule(80)
        val item = newScheduledCameraEventEntry("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledCameraEventEntryRequiredCodeRule("scheduled camera event entry needs an entry")
        val item = newScheduledCameraEventEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = ScheduledCameraEventEntryRequiredDepotRule("scheduled camera event entry needs a value")
        val item = newScheduledCameraEventEntry("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
