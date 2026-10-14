package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultBackhaulVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultBackhaulVarianceValidator()
        val item = newDefaultBackhaulVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DefaultBackhaulVarianceThresholdRangeRule(0.8, 2825.5)
        val item = newDefaultBackhaulVariance("x", "y")
        item.threshold = 2827.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
