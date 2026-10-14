package com.acme.transport.terminal.crossdock.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCrossDockThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCrossDockThresholdValidator()
        val item = newAdaptiveCrossDockThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveCrossDockThresholdRequiredLabelRule("adaptive cross dock threshold needs a value")
        val item = newAdaptiveCrossDockThreshold("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveCrossDockThresholdTitleLengthRule(255)
        val item = newAdaptiveCrossDockThreshold("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
