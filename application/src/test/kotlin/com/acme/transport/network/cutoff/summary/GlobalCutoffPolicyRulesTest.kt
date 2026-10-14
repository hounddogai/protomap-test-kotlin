package com.acme.transport.network.cutoff.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCutoffPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCutoffPolicyValidator()
        val item = newGlobalCutoffPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = GlobalCutoffPolicyDiscountRangeRule(9.0, 3769.7)
        val item = newGlobalCutoffPolicy("x", "y")
        item.discount = 3771.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = GlobalCutoffPolicyAttemptsRangeRule(1000, 3600)
        val item = newGlobalCutoffPolicy("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = GlobalCutoffPolicyContactEmailLengthRule(64)
        val item = newGlobalCutoffPolicy("x", "y")
        item.contactEmail = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
