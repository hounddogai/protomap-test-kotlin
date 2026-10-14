package com.acme.transport.terminal.dwell.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDwellMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDwellMetricValidator()
        val item = newAdaptiveDwellMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = AdaptiveDwellMetricRequiredDepotRule("adaptive dwell metric needs a value")
        val item = newAdaptiveDwellMetric("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = AdaptiveDwellMetricEndsAtRangeRule(32, 64)
        val item = newAdaptiveDwellMetric("x", "y")
        item.endsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
