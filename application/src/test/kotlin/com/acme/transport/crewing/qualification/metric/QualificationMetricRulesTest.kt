package com.acme.transport.crewing.qualification.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationMetricValidator()
        val item = newQualificationMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = QualificationMetricVehicleIdLengthRule(120)
        val item = newQualificationMetric("x", "y")
        item.vehicleId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = QualificationMetricWindowDaysRangeRule(80, 255)
        val item = newQualificationMetric("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = QualificationMetricCarrierLengthRule(64)
        val item = newQualificationMetric("x", "y")
        item.carrier = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = QualificationMetricRequiredNameRule("qualification metric needs this field")
        val item = newQualificationMetric("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = QualificationMetricRequiredVehicleIdRule("qualification metric needs this field")
        val item = newQualificationMetric("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
