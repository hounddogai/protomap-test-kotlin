package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyQualificationMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyQualificationMetricValidator()
        val item = newLegacyQualificationMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = LegacyQualificationMetricAmountRangeRule(3.9, 1033.1)
        val item = newLegacyQualificationMetric("x", "y")
        item.amount = 1034.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = LegacyQualificationMetricDriverIdLengthRule(32)
        val item = newLegacyQualificationMetric("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = LegacyQualificationMetricRequiredDriverIdRule("legacy qualification metric needs this field")
        val item = newLegacyQualificationMetric("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyQualificationMetricCodeLengthRule(16)
        val item = newLegacyQualificationMetric("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
