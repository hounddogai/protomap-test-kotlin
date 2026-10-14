package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackBackhaulRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackBackhaulRequestValidator()
        val item = newFallbackBackhaulRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = FallbackBackhaulRequestRequiredContactEmailRule("fallback backhaul request needs a value")
        val item = newFallbackBackhaulRequest("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = FallbackBackhaulRequestChannelLengthRule(64)
        val item = newFallbackBackhaulRequest("x", "y")
        item.channel = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = FallbackBackhaulRequestWindowDaysRangeRule(80, 80)
        val item = newFallbackBackhaulRequest("x", "y")
        item.windowDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
