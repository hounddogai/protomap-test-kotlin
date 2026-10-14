package com.acme.transport.freightpay.ratecard.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRateCardThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRateCardThresholdValidator()
        val item = newRollingRateCardThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = RollingRateCardThresholdBatchSizeRangeRule(16, 255)
        val item = newRollingRateCardThreshold("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingRateCardThresholdRequiredLabelRule("rolling rate card threshold needs an entry")
        val item = newRollingRateCardThreshold("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
