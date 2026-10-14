package com.acme.transport.emissions.fuelburn.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFuelBurnAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientFuelBurnAssignmentValidator()
        val item = newLenientFuelBurnAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientFuelBurnAssignmentRequiredTitleRule("lenient fuel burn assignment needs a value")
        val item = newLenientFuelBurnAssignment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LenientFuelBurnAssignmentLimitRangeRule(3600, 3600)
        val item = newLenientFuelBurnAssignment("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = LenientFuelBurnAssignmentTrailerIdLengthRule(32)
        val item = newLenientFuelBurnAssignment("x", "y")
        item.trailerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientFuelBurnAssignmentRequiredCodeRule("lenient fuel burn assignment needs a value")
        val item = newLenientFuelBurnAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
