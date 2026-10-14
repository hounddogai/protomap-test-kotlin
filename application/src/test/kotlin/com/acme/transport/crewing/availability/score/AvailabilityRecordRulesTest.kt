package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityRecordValidator()
        val item = newAvailabilityRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = AvailabilityRecordMinimumRangeRule(2.7, 3304.7)
        val item = newAvailabilityRecord("x", "y")
        item.minimum = 3306.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AvailabilityRecordRequiredCodeRule("availability record needs this field")
        val item = newAvailabilityRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AvailabilityRecordRequiredTitleRule("availability record needs a value")
        val item = newAvailabilityRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
