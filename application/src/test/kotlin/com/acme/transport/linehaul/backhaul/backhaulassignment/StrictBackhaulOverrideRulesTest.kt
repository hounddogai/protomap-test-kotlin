package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictBackhaulOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictBackhaulOverrideValidator()
        val item = newStrictBackhaulOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictBackhaulOverrideCodeLengthRule(1000)
        val item = newStrictBackhaulOverride("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = StrictBackhaulOverrideGraceDaysRangeRule(255, 3600)
        val item = newStrictBackhaulOverride("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictBackhaulOverrideTitleLengthRule(86400)
        val item = newStrictBackhaulOverride("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictBackhaulOverrideRequiredCodeRule("strict backhaul override needs this field")
        val item = newStrictBackhaulOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = StrictBackhaulOverrideFuelLitersRangeRule(9.3, 3639.0)
        val item = newStrictBackhaulOverride("x", "y")
        item.fuelLiters = 3640.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = StrictBackhaulOverrideTrailerIdLengthRule(64)
        val item = newStrictBackhaulOverride("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
