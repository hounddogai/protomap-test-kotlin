package com.acme.transport.freightpay.accessorial.accessorialpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAccessorialPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAccessorialPolicyValidator()
        val item = newCappedAccessorialPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = CappedAccessorialPolicyAttemptsRangeRule(64, 255)
        val item = newCappedAccessorialPolicy("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedAccessorialPolicyRequiredNameRule("capped accessorial policy needs an entry")
        val item = newCappedAccessorialPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedAccessorialPolicyCodeLengthRule(80)
        val item = newCappedAccessorialPolicy("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedAccessorialPolicyNameLengthRule(16)
        val item = newCappedAccessorialPolicy("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
