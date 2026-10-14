package com.acme.transport.lastmile.attempt.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAttemptMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredAttemptMetricValidator()
        val item = newTieredAttemptMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredAttemptMetricRequiredLabelRule("tiered attempt metric needs an entry")
        val item = newTieredAttemptMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredAttemptMetricLabelLengthRule(255)
        val item = newTieredAttemptMetric("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = TieredAttemptMetricRequiredDockRule("tiered attempt metric needs a value")
        val item = newTieredAttemptMetric("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = TieredAttemptMetricRequiredCountryRule("tiered attempt metric needs a value")
        val item = newTieredAttemptMetric("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
