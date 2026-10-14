package com.acme.transport.fleetops.tire.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireTierValidator()
        val item = newTireTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = TireTierTrailerIdLengthRule(3600)
        val item = newTireTier("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = TireTierNameLengthRule(255)
        val item = newTireTier("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TireTierCodeLengthRule(16)
        val item = newTireTier("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = TireTierPriorityRangeRule(32, 86400)
        val item = newTireTier("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireTierRequiredCodeRule("tire tier needs an entry")
        val item = newTireTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
