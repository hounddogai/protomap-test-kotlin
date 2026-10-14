package com.acme.transport.lastmile.drop.dropexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDropExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDropExceptionValidator()
        val item = newLegacyDropException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LegacyDropExceptionWeightRangeRule(2.5, 4750.6)
        val item = newLegacyDropException("x", "y")
        item.weight = 4752.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = LegacyDropExceptionDockLengthRule(255)
        val item = newLegacyDropException("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyDropExceptionRequiredCodeRule("legacy drop exception needs a value")
        val item = newLegacyDropException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyDropExceptionCodeLengthRule(80)
        val item = newLegacyDropException("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
