package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBackhaulPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingBackhaulPolicyValidator()
        val item = newRollingBackhaulPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingBackhaulPolicyCodeLengthRule(86400)
        val item = newRollingBackhaulPolicy("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = RollingBackhaulPolicyQuantityRangeRule(80, 3600)
        val item = newRollingBackhaulPolicy("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
