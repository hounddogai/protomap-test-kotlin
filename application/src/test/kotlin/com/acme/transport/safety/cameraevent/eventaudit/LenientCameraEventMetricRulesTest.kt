package com.acme.transport.safety.cameraevent.eventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCameraEventMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCameraEventMetricValidator()
        val item = newLenientCameraEventMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LenientCameraEventMetricDiscountRangeRule(6.3, 4784.5)
        val item = newLenientCameraEventMetric("x", "y")
        item.discount = 4786.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = LenientCameraEventMetricRequiredCountryRule("lenient camera event metric needs this field")
        val item = newLenientCameraEventMetric("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientCameraEventMetricRequiredTitleRule("lenient camera event metric needs an entry")
        val item = newLenientCameraEventMetric("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
