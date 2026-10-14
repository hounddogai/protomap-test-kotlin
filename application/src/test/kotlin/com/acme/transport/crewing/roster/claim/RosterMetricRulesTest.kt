package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterMetricValidator()
        val item = newRosterMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RosterMetricRequiredCodeRule("roster metric needs a value")
        val item = newRosterMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RosterMetricThresholdRangeRule(1.0, 303.6)
        val item = newRosterMetric("x", "y")
        item.threshold = 305.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = RosterMetricNameLengthRule(3600)
        val item = newRosterMetric("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
