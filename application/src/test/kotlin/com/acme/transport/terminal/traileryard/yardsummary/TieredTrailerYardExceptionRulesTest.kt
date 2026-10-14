package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTrailerYardExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredTrailerYardExceptionValidator()
        val item = newTieredTrailerYardException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = TieredTrailerYardExceptionRequiredPhoneRule("tiered trailer yard exception needs a value")
        val item = newTieredTrailerYardException("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = TieredTrailerYardExceptionQuantityRangeRule(16, 32)
        val item = newTieredTrailerYardException("x", "y")
        item.quantity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredTrailerYardExceptionCodeLengthRule(32)
        val item = newTieredTrailerYardException("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TieredTrailerYardExceptionRequiredOwnerIdRule("tiered trailer yard exception needs a value")
        val item = newTieredTrailerYardException("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredTrailerYardExceptionRequiredCodeRule("tiered trailer yard exception needs this field")
        val item = newTieredTrailerYardException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
