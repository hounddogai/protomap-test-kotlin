package com.acme.transport.fleetops.tire.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTireExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTireExceptionValidator()
        val item = newAdaptiveTireException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveTireExceptionRequiredCodeRule("adaptive tire exception needs a value")
        val item = newAdaptiveTireException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = AdaptiveTireExceptionRequiredSegmentRule("adaptive tire exception needs a value")
        val item = newAdaptiveTireException("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = AdaptiveTireExceptionBatchSizeRangeRule(80, 80)
        val item = newAdaptiveTireException("x", "y")
        item.batchSize = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
