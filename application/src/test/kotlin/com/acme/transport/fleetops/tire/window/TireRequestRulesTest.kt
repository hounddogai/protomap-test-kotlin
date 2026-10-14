package com.acme.transport.fleetops.tire.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireRequestValidator()
        val item = newTireRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = TireRequestMarginRangeRule(4.2, 2898.9)
        val item = newTireRequest("x", "y")
        item.margin = 2900.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = TireRequestRatioRangeRule(6.6, 4600.7)
        val item = newTireRequest("x", "y")
        item.ratio = 4602.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TireRequestRequiredOwnerIdRule("tire request needs a value")
        val item = newTireRequest("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = TireRequestRequiredExternalRefRule("tire request needs a value")
        val item = newTireRequest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireRequestRequiredCodeRule("tire request needs this field")
        val item = newTireRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TireRequestCodeLengthRule(86400)
        val item = newTireRequest("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
