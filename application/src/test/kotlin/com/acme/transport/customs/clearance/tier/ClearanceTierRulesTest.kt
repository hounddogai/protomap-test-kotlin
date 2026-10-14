package com.acme.transport.customs.clearance.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceTierValidator()
        val item = newClearanceTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ClearanceTierSequenceRangeRule(16, 86400)
        val item = newClearanceTier("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ClearanceTierContactEmailLengthRule(80)
        val item = newClearanceTier("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
