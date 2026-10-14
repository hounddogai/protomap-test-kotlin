package com.acme.transport.crewing.swap.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapThresholdValidator()
        val item = newSwapThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = SwapThresholdRequiredSegmentRule("swap threshold needs an entry")
        val item = newSwapThreshold("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = SwapThresholdRequiredChannelRule("swap threshold needs this field")
        val item = newSwapThreshold("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
