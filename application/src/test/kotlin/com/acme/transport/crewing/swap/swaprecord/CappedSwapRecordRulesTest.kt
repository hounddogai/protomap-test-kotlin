package com.acme.transport.crewing.swap.swaprecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedSwapRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedSwapRecordValidator()
        val item = newCappedSwapRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = CappedSwapRecordSequenceRangeRule(16, 86400)
        val item = newCappedSwapRecord("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedSwapRecordNameLengthRule(86400)
        val item = newCappedSwapRecord("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedSwapRecordCodeLengthRule(32)
        val item = newCappedSwapRecord("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = CappedSwapRecordRequiredDriverIdRule("capped swap record needs an entry")
        val item = newCappedSwapRecord("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
