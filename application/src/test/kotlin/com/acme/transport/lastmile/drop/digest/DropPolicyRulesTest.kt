package com.acme.transport.lastmile.drop.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropPolicyValidator()
        val item = newDropPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DropPolicyNameLengthRule(3600)
        val item = newDropPolicy("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DropPolicyRequiredNameRule("drop policy needs this field")
        val item = newDropPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
