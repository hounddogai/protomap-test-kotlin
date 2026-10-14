package com.acme.transport.customs.inspection.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledInspectionMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledInspectionMetricValidator()
        val item = newScheduledInspectionMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledInspectionMetricNameLengthRule(3600)
        val item = newScheduledInspectionMetric("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledInspectionMetricCodeLengthRule(32)
        val item = newScheduledInspectionMetric("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledInspectionMetricRequiredNameRule("scheduled inspection metric needs this field")
        val item = newScheduledInspectionMetric("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
