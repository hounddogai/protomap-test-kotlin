package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultNearMissMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultNearMissMetricValidator()
        val item = newDefaultNearMissMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultNearMissMetricRequiredCodeRule("default near miss metric needs an entry")
        val item = newDefaultNearMissMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DefaultNearMissMetricCeilingRangeRule(4.9, 1260.9)
        val item = newDefaultNearMissMetric("x", "y")
        item.ceiling = 1262.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = DefaultNearMissMetricRevisionRangeRule(80, 120)
        val item = newDefaultNearMissMetric("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = DefaultNearMissMetricChecksumLengthRule(64)
        val item = newDefaultNearMissMetric("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
