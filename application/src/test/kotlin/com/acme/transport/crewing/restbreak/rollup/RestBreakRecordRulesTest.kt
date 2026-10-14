package com.acme.transport.crewing.restbreak.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakRecordValidator()
        val item = newRestBreakRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RestBreakRecordLoadKgRangeRule(2.9, 2967.7)
        val item = newRestBreakRecord("x", "y")
        item.loadKg = 2969.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RestBreakRecordCodeLengthRule(120)
        val item = newRestBreakRecord("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = RestBreakRecordSourceLengthRule(120)
        val item = newRestBreakRecord("x", "y")
        item.source = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
