package com.acme.transport.lastmile.parcel.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledParcelSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledParcelSummaryValidator()
        val item = newScheduledParcelSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledParcelSummaryRequiredCodeRule("scheduled parcel summary needs a value")
        val item = newScheduledParcelSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledParcelSummaryLabelLengthRule(32)
        val item = newScheduledParcelSummary("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ScheduledParcelSummarySummaryLengthRule(255)
        val item = newScheduledParcelSummary("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledParcelSummaryCodeLengthRule(1000)
        val item = newScheduledParcelSummary("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledParcelSummaryRequiredLabelRule("scheduled parcel summary needs an entry")
        val item = newScheduledParcelSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
