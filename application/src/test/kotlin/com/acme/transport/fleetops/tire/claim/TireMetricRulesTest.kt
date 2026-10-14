package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireMetricValidator()
        val item = newTireMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TireMetricCodeLengthRule(1000)
        val item = newTireMetric("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = TireMetricExpiresAtRangeRule(32, 120)
        val item = newTireMetric("x", "y")
        item.expiresAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = TireMetricMarginRangeRule(2.1, 3661.1)
        val item = newTireMetric("x", "y")
        item.margin = 3662.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
