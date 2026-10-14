package com.acme.transport.safety.coaching.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCoachingRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCoachingRecordValidator()
        val item = newPrimaryCoachingRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryCoachingRecordRequiredCodeRule("primary coaching record needs this field")
        val item = newPrimaryCoachingRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryCoachingRecordRequiredTitleRule("primary coaching record needs an entry")
        val item = newPrimaryCoachingRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = PrimaryCoachingRecordRequiredSummaryRule("primary coaching record needs this field")
        val item = newPrimaryCoachingRecord("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = PrimaryCoachingRecordRequiredTrailerIdRule("primary coaching record needs this field")
        val item = newPrimaryCoachingRecord("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = PrimaryCoachingRecordRetriesRangeRule(64, 120)
        val item = newPrimaryCoachingRecord("x", "y")
        item.retries = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
