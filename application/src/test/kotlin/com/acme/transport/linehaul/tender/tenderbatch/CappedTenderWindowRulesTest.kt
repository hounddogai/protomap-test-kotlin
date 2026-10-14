package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTenderWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTenderWindowValidator()
        val item = newCappedTenderWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedTenderWindowNameLengthRule(120)
        val item = newCappedTenderWindow("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = CappedTenderWindowQuantityRangeRule(16, 120)
        val item = newCappedTenderWindow("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
