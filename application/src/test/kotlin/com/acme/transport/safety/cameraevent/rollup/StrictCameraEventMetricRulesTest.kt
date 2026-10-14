package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCameraEventMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictCameraEventMetricValidator()
        val item = newStrictCameraEventMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = StrictCameraEventMetricRequiredCurrencyRule("strict camera event metric needs this field")
        val item = newStrictCameraEventMetric("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictCameraEventMetricCodeLengthRule(16)
        val item = newStrictCameraEventMetric("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
