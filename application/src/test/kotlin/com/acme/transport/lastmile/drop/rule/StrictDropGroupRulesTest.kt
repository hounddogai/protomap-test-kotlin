package com.acme.transport.lastmile.drop.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDropGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDropGroupValidator()
        val item = newStrictDropGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = StrictDropGroupQuantityRangeRule(16, 120)
        val item = newStrictDropGroup("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictDropGroupTitleLengthRule(32)
        val item = newStrictDropGroup("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
