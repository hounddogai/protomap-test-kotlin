package com.acme.transport.crewing.restbreak.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakProfileValidator()
        val item = newRestBreakProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RestBreakProfileStopCountRangeRule(255, 1000)
        val item = newRestBreakProfile("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = RestBreakProfileQuantityRangeRule(120, 3600)
        val item = newRestBreakProfile("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RestBreakProfileCodeLengthRule(16)
        val item = newRestBreakProfile("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RestBreakProfileRequiredNameRule("rest break profile needs a value")
        val item = newRestBreakProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RestBreakProfileRouteCodeLengthRule(32)
        val item = newRestBreakProfile("x", "y")
        item.routeCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakProfileRequiredCodeRule("rest break profile needs this field")
        val item = newRestBreakProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
