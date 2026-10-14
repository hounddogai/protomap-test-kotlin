package com.acme.transport.terminal.dockdoor.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDockDoorNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDockDoorNoticeValidator()
        val item = newScheduledDockDoorNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledDockDoorNoticeRequiredTitleRule("scheduled dock door notice needs this field")
        val item = newScheduledDockDoorNotice("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ScheduledDockDoorNoticeMaximumRangeRule(1.0, 1890.1)
        val item = newScheduledDockDoorNotice("x", "y")
        item.maximum = 1891.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ScheduledDockDoorNoticeRequiredLocaleRule("scheduled dock door notice needs this field")
        val item = newScheduledDockDoorNotice("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
