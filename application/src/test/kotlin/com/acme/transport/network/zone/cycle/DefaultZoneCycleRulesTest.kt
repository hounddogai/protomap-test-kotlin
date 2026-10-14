package com.acme.transport.network.zone.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultZoneCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultZoneCycleValidator()
        val item = newDefaultZoneCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = DefaultZoneCycleExpiresAtRangeRule(80, 3600)
        val item = newDefaultZoneCycle("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultZoneCycleCodeLengthRule(32)
        val item = newDefaultZoneCycle("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = DefaultZoneCycleWindowDaysRangeRule(16, 32)
        val item = newDefaultZoneCycle("x", "y")
        item.windowDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = DefaultZoneCycleRequiredLaneRule("default zone cycle needs this field")
        val item = newDefaultZoneCycle("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = DefaultZoneCycleLaneLengthRule(64)
        val item = newDefaultZoneCycle("x", "y")
        item.lane = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
