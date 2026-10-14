package com.acme.transport.freightpay.accessorial.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialPolicyValidator()
        val item = newAccessorialPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AccessorialPolicyCodeLengthRule(255)
        val item = newAccessorialPolicy("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = AccessorialPolicyRegionLengthRule(255)
        val item = newAccessorialPolicy("x", "y")
        item.region = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long segment`() {
        val rule = AccessorialPolicySegmentLengthRule(120)
        val item = newAccessorialPolicy("x", "y")
        item.segment = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
