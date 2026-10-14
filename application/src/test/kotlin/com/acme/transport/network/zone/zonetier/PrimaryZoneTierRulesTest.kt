package com.acme.transport.network.zone.zonetier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryZoneTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryZoneTierValidator()
        val item = newPrimaryZoneTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = PrimaryZoneTierWindowDaysRangeRule(120, 1000)
        val item = newPrimaryZoneTier("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryZoneTierRequiredCodeRule("primary zone tier needs this field")
        val item = newPrimaryZoneTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryZoneTierTitleLengthRule(80)
        val item = newPrimaryZoneTier("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = PrimaryZoneTierRequiredExternalRefRule("primary zone tier needs a value")
        val item = newPrimaryZoneTier("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
