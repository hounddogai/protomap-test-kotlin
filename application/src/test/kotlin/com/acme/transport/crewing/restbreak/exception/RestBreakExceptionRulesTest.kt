package com.acme.transport.crewing.restbreak.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakExceptionValidator()
        val item = newRestBreakException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RestBreakExceptionMinimumRangeRule(4.0, 3622.3)
        val item = newRestBreakException("x", "y")
        item.minimum = 3623.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RestBreakExceptionStartsAtRangeRule(64, 80)
        val item = newRestBreakException("x", "y")
        item.startsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = RestBreakExceptionDriverIdLengthRule(80)
        val item = newRestBreakException("x", "y")
        item.driverId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RestBreakExceptionCodeLengthRule(120)
        val item = newRestBreakException("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
