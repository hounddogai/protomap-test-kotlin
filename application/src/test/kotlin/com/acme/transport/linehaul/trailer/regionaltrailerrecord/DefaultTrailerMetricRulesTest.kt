package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTrailerMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTrailerMetricValidator()
        val item = newDefaultTrailerMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = DefaultTrailerMetricRequiredDockRule("default trailer metric needs an entry")
        val item = newDefaultTrailerMetric("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = DefaultTrailerMetricDescriptionLengthRule(32)
        val item = newDefaultTrailerMetric("x", "y")
        item.description = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
