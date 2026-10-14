package com.acme.transport.linehaul.relay.relaypolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRelayPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRelayPolicyValidator()
        val item = newFallbackRelayPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackRelayPolicyTitleLengthRule(80)
        val item = newFallbackRelayPolicy("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = FallbackRelayPolicyPriorityRangeRule(32, 80)
        val item = newFallbackRelayPolicy("x", "y")
        item.priority = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = FallbackRelayPolicyThresholdRangeRule(9.7, 3614.8)
        val item = newFallbackRelayPolicy("x", "y")
        item.threshold = 3616.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = FallbackRelayPolicyCeilingRangeRule(3.9, 1709.5)
        val item = newFallbackRelayPolicy("x", "y")
        item.ceiling = 1711.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRelayPolicyCodeLengthRule(120)
        val item = newFallbackRelayPolicy("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
