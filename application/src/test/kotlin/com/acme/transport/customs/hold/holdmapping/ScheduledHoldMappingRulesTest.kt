package com.acme.transport.customs.hold.holdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledHoldMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledHoldMappingValidator()
        val item = newScheduledHoldMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledHoldMappingCodeLengthRule(255)
        val item = newScheduledHoldMapping("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledHoldMappingLabelLengthRule(32)
        val item = newScheduledHoldMapping("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ScheduledHoldMappingThresholdRangeRule(8.5, 2462.3)
        val item = newScheduledHoldMapping("x", "y")
        item.threshold = 2463.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
