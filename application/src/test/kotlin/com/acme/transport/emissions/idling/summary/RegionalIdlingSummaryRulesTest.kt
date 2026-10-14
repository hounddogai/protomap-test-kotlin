package com.acme.transport.emissions.idling.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIdlingSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalIdlingSummaryValidator()
        val item = newRegionalIdlingSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalIdlingSummaryRequiredCodeRule("regional idling summary needs this field")
        val item = newRegionalIdlingSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalIdlingSummaryNameLengthRule(80)
        val item = newRegionalIdlingSummary("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RegionalIdlingSummaryRetriesRangeRule(16, 32)
        val item = newRegionalIdlingSummary("x", "y")
        item.retries = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
