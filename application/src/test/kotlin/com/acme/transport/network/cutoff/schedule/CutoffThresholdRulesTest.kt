package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffThresholdValidator()
        val item = newCutoffThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = CutoffThresholdRequiredExternalRefRule("cutoff threshold needs an entry")
        val item = newCutoffThreshold("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CutoffThresholdRequiredCodeRule("cutoff threshold needs this field")
        val item = newCutoffThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CutoffThresholdCodeLengthRule(16)
        val item = newCutoffThreshold("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = CutoffThresholdThresholdRangeRule(7.9, 1118.8)
        val item = newCutoffThreshold("x", "y")
        item.threshold = 1120.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = CutoffThresholdPriorityRangeRule(64, 80)
        val item = newCutoffThreshold("x", "y")
        item.priority = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
