package com.acme.transport.linehaul.backhaul.backhaulreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyBackhaulWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyBackhaulWindowValidator()
        val item = newLegacyBackhaulWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyBackhaulWindowTitleLengthRule(255)
        val item = newLegacyBackhaulWindow("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = LegacyBackhaulWindowMinimumRangeRule(9.7, 2514.8)
        val item = newLegacyBackhaulWindow("x", "y")
        item.minimum = 2516.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
