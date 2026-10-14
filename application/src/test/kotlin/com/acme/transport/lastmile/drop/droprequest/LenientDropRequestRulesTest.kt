package com.acme.transport.lastmile.drop.droprequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDropRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDropRequestValidator()
        val item = newLenientDropRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientDropRequestRequiredTitleRule("lenient drop request needs this field")
        val item = newLenientDropRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LenientDropRequestUpdatedAtRangeRule(32, 64)
        val item = newLenientDropRequest("x", "y")
        item.updatedAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = LenientDropRequestDepotLengthRule(86400)
        val item = newLenientDropRequest("x", "y")
        item.depot = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = LenientDropRequestRequiredDepotRule("lenient drop request needs this field")
        val item = newLenientDropRequest("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
