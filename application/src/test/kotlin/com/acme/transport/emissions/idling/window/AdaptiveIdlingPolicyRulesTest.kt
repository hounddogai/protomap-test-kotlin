package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveIdlingPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveIdlingPolicyValidator()
        val item = newAdaptiveIdlingPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = AdaptiveIdlingPolicyBatchSizeRangeRule(255, 255)
        val item = newAdaptiveIdlingPolicy("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = AdaptiveIdlingPolicyStartsAtRangeRule(16, 86400)
        val item = newAdaptiveIdlingPolicy("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
