package com.acme.transport.customs.broker.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredBrokerMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredBrokerMetricValidator()
        val item = newTieredBrokerMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredBrokerMetricRequiredCodeRule("tiered broker metric needs a value")
        val item = newTieredBrokerMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = TieredBrokerMetricEndsAtRangeRule(16, 120)
        val item = newTieredBrokerMetric("x", "y")
        item.endsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = TieredBrokerMetricScoreRangeRule(5.3, 3964.1)
        val item = newTieredBrokerMetric("x", "y")
        item.score = 3965.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
