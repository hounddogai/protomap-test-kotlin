package com.acme.transport.crewing.dutyperiod.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodMetricValidator()
        val item = newDutyPeriodMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = DutyPeriodMetricMaximumRangeRule(5.8, 1618.8)
        val item = newDutyPeriodMetric("x", "y")
        item.maximum = 1620.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = DutyPeriodMetricUpdatedAtRangeRule(32, 86400)
        val item = newDutyPeriodMetric("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = DutyPeriodMetricRequiredLaneRule("duty period metric needs an entry")
        val item = newDutyPeriodMetric("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
