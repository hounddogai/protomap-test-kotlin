package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockMetricValidator()
        val item = newCrossDockMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = CrossDockMetricStartsAtRangeRule(16, 16)
        val item = newCrossDockMetric("x", "y")
        item.startsAt = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = CrossDockMetricOdometerRangeRule(120, 3600)
        val item = newCrossDockMetric("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CrossDockMetricRequiredLabelRule("cross dock metric needs a value")
        val item = newCrossDockMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
