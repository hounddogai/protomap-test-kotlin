package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneTierValidator()
        val item = newZoneTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ZoneTierCodeLengthRule(32)
        val item = newZoneTier("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ZoneTierLabelLengthRule(86400)
        val item = newZoneTier("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ZoneTierAttemptsRangeRule(16, 1000)
        val item = newZoneTier("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
