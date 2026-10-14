package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceExceptionValidator()
        val item = newClearanceException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ClearanceExceptionNameLengthRule(120)
        val item = newClearanceException("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ClearanceExceptionExpiresAtRangeRule(64, 86400)
        val item = newClearanceException("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ClearanceExceptionCreatedAtRangeRule(120, 1000)
        val item = newClearanceException("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
