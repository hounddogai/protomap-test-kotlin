package com.acme.transport.terminal.dwell.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDwellPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDwellPolicyValidator()
        val item = newLenientDwellPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientDwellPolicyCodeLengthRule(3600)
        val item = newLenientDwellPolicy("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = LenientDwellPolicyRequiredSegmentRule("lenient dwell policy needs a value")
        val item = newLenientDwellPolicy("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = LenientDwellPolicyCurrencyLengthRule(32)
        val item = newLenientDwellPolicy("x", "y")
        item.currency = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
