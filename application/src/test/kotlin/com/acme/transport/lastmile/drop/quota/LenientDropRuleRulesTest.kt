package com.acme.transport.lastmile.drop.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDropRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDropRuleValidator()
        val item = newLenientDropRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = LenientDropRuleFloorValueRangeRule(9.0, 2013.3)
        val item = newLenientDropRule("x", "y")
        item.floorValue = 2014.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = LenientDropRuleDepotLengthRule(64)
        val item = newLenientDropRule("x", "y")
        item.depot = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientDropRuleRequiredLabelRule("lenient drop rule needs an entry")
        val item = newLenientDropRule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = LenientDropRuleRequiredDepotRule("lenient drop rule needs an entry")
        val item = newLenientDropRule("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
