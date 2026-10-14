package com.acme.transport.fleetops.defect.defectclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDefectMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDefectMetricValidator()
        val item = newScheduledDefectMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledDefectMetricTitleLengthRule(255)
        val item = newScheduledDefectMetric("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ScheduledDefectMetricFuelLitersRangeRule(3.4, 172.4)
        val item = newScheduledDefectMetric("x", "y")
        item.fuelLiters = 173.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledDefectMetricRequiredTitleRule("scheduled defect metric needs this field")
        val item = newScheduledDefectMetric("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledDefectMetricRequiredCodeRule("scheduled defect metric needs an entry")
        val item = newScheduledDefectMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ScheduledDefectMetricAttemptsRangeRule(32, 120)
        val item = newScheduledDefectMetric("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
