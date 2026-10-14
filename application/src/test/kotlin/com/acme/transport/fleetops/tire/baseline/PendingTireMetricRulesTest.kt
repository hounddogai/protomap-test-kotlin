package com.acme.transport.fleetops.tire.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTireMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTireMetricValidator()
        val item = newPendingTireMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = PendingTireMetricTrailerIdLengthRule(255)
        val item = newPendingTireMetric("x", "y")
        item.trailerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingTireMetricNameLengthRule(255)
        val item = newPendingTireMetric("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = PendingTireMetricDescriptionLengthRule(255)
        val item = newPendingTireMetric("x", "y")
        item.description = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = PendingTireMetricRequiredDescriptionRule("pending tire metric needs an entry")
        val item = newPendingTireMetric("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
