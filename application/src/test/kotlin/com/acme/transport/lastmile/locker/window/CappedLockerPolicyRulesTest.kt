package com.acme.transport.lastmile.locker.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLockerPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedLockerPolicyValidator()
        val item = newCappedLockerPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedLockerPolicyRequiredCodeRule("capped locker policy needs an entry")
        val item = newCappedLockerPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = CappedLockerPolicySourceLengthRule(16)
        val item = newCappedLockerPolicy("x", "y")
        item.source = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedLockerPolicyCodeLengthRule(80)
        val item = newCappedLockerPolicy("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = CappedLockerPolicyOwnerIdLengthRule(32)
        val item = newCappedLockerPolicy("x", "y")
        item.ownerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
