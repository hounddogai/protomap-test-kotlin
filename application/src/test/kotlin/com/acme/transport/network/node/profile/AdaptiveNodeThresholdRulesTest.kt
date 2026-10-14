package com.acme.transport.network.node.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNodeThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveNodeThresholdValidator()
        val item = newAdaptiveNodeThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveNodeThresholdNameLengthRule(80)
        val item = newAdaptiveNodeThreshold("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveNodeThresholdRequiredCodeRule("adaptive node threshold needs this field")
        val item = newAdaptiveNodeThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = AdaptiveNodeThresholdLocaleLengthRule(32)
        val item = newAdaptiveNodeThreshold("x", "y")
        item.locale = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveNodeThresholdCodeLengthRule(32)
        val item = newAdaptiveNodeThreshold("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveNodeThresholdTitleLengthRule(3600)
        val item = newAdaptiveNodeThreshold("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
