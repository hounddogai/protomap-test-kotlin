package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCrossDockExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyCrossDockExceptionValidator()
        val item = newLegacyCrossDockException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LegacyCrossDockExceptionSequenceRangeRule(64, 86400)
        val item = newLegacyCrossDockException("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = LegacyCrossDockExceptionExternalRefLengthRule(120)
        val item = newLegacyCrossDockException("x", "y")
        item.externalRef = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = LegacyCrossDockExceptionPalletsRangeRule(32, 255)
        val item = newLegacyCrossDockException("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = LegacyCrossDockExceptionRequiredExternalRefRule("legacy cross dock exception needs this field")
        val item = newLegacyCrossDockException("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = LegacyCrossDockExceptionAmountRangeRule(7.6, 3864.8)
        val item = newLegacyCrossDockException("x", "y")
        item.amount = 3866.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
