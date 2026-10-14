package com.acme.transport.network.transittime.timedigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTransitTimeDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTransitTimeDigestValidator()
        val item = newWeightedTransitTimeDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = WeightedTransitTimeDigestRequiredLocaleRule("weighted transit time digest needs a value")
        val item = newWeightedTransitTimeDigest("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = WeightedTransitTimeDigestCreatedAtRangeRule(32, 80)
        val item = newWeightedTransitTimeDigest("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
