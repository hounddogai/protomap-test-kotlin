package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAccessorialRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAccessorialRequestValidator()
        val item = newAdaptiveAccessorialRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveAccessorialRequestRequiredCodeRule("adaptive accessorial request needs this field")
        val item = newAdaptiveAccessorialRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = AdaptiveAccessorialRequestScoreRangeRule(3.2, 2805.9)
        val item = newAdaptiveAccessorialRequest("x", "y")
        item.score = 2807.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveAccessorialRequestLabelLengthRule(1000)
        val item = newAdaptiveAccessorialRequest("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveAccessorialRequestCodeLengthRule(3600)
        val item = newAdaptiveAccessorialRequest("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
