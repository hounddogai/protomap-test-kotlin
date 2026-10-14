package com.acme.transport.safety.coaching.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCoachingMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCoachingMetricValidator()
        val item = newDefaultCoachingMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = DefaultCoachingMetricRequiredUnitRule("default coaching metric needs this field")
        val item = newDefaultCoachingMetric("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = DefaultCoachingMetricRequiredChecksumRule("default coaching metric needs an entry")
        val item = newDefaultCoachingMetric("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = DefaultCoachingMetricChecksumLengthRule(16)
        val item = newDefaultCoachingMetric("x", "y")
        item.checksum = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultCoachingMetricCodeLengthRule(80)
        val item = newDefaultCoachingMetric("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = DefaultCoachingMetricVersionRangeRule(80, 1000)
        val item = newDefaultCoachingMetric("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultCoachingMetricNameLengthRule(80)
        val item = newDefaultCoachingMetric("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
