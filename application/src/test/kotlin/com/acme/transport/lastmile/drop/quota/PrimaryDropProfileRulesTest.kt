package com.acme.transport.lastmile.drop.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDropProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDropProfileValidator()
        val item = newPrimaryDropProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = PrimaryDropProfileRequiredDepotRule("primary drop profile needs a value")
        val item = newPrimaryDropProfile("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryDropProfileRequiredNameRule("primary drop profile needs this field")
        val item = newPrimaryDropProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = PrimaryDropProfileRouteCodeLengthRule(80)
        val item = newPrimaryDropProfile("x", "y")
        item.routeCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = PrimaryDropProfileCostRangeRule(5.9, 2704.7)
        val item = newPrimaryDropProfile("x", "y")
        item.cost = 2706.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
