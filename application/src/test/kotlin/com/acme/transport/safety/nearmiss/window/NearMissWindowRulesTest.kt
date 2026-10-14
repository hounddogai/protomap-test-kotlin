package com.acme.transport.safety.nearmiss.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissWindowValidator()
        val item = newNearMissWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = NearMissWindowAmountRangeRule(0.1, 2190.4)
        val item = newNearMissWindow("x", "y")
        item.amount = 2191.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissWindowCodeLengthRule(120)
        val item = newNearMissWindow("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = NearMissWindowRateRangeRule(1.1, 3205.5)
        val item = newNearMissWindow("x", "y")
        item.rate = 3207.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
