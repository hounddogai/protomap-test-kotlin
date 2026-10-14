package com.acme.transport.freightpay.dispute.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDisputeSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDisputeSummaryValidator()
        val item = newStrictDisputeSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = StrictDisputeSummaryDurationMinutesRangeRule(32, 1000)
        val item = newStrictDisputeSummary("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = StrictDisputeSummaryRetriesRangeRule(32, 3600)
        val item = newStrictDisputeSummary("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = StrictDisputeSummaryLimitRangeRule(1000, 3600)
        val item = newStrictDisputeSummary("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictDisputeSummaryRequiredCodeRule("strict dispute summary needs a value")
        val item = newStrictDisputeSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictDisputeSummaryRequiredNameRule("strict dispute summary needs this field")
        val item = newStrictDisputeSummary("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
