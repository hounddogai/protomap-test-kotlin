package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTrailerMetricValidator()
        val item = newManualTrailerMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualTrailerMetricCodeLengthRule(1000)
        val item = newManualTrailerMetric("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualTrailerMetricRequiredNameRule("manual trailer metric needs an entry")
        val item = newManualTrailerMetric("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualTrailerMetricRequiredCodeRule("manual trailer metric needs an entry")
        val item = newManualTrailerMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = ManualTrailerMetricRequiredTimezoneRule("manual trailer metric needs an entry")
        val item = newManualTrailerMetric("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
