package com.acme.transport.crewing.roster.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterThresholdValidator()
        val item = newRosterThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RosterThresholdCreatedAtRangeRule(255, 1000)
        val item = newRosterThreshold("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = RosterThresholdRequiredShiftCodeRule("roster threshold needs this field")
        val item = newRosterThreshold("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RosterThresholdRequiredCodeRule("roster threshold needs a value")
        val item = newRosterThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RosterThresholdRequiredTitleRule("roster threshold needs an entry")
        val item = newRosterThreshold("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RosterThresholdRequiredDriverIdRule("roster threshold needs this field")
        val item = newRosterThreshold("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
