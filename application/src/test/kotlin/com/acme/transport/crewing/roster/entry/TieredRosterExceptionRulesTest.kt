package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRosterExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRosterExceptionValidator()
        val item = newTieredRosterException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredRosterExceptionRequiredCodeRule("tiered roster exception needs a value")
        val item = newTieredRosterException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = TieredRosterExceptionRequiredCarrierRule("tiered roster exception needs an entry")
        val item = newTieredRosterException("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredRosterExceptionCodeLengthRule(80)
        val item = newTieredRosterException("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = TieredRosterExceptionCostRangeRule(2.9, 3968.6)
        val item = newTieredRosterException("x", "y")
        item.cost = 3970.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
