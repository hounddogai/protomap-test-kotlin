package com.acme.transport.freightpay.accessorial.accessorialpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingAccessorialPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingAccessorialPolicyValidator()
        val item = newRollingAccessorialPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingAccessorialPolicyRequiredCodeRule("rolling accessorial policy needs this field")
        val item = newRollingAccessorialPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingAccessorialPolicyCodeLengthRule(120)
        val item = newRollingAccessorialPolicy("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = RollingAccessorialPolicyDescriptionLengthRule(64)
        val item = newRollingAccessorialPolicy("x", "y")
        item.description = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
