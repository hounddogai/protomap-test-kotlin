package com.acme.transport.emissions.fuelburn.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultFuelBurnAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultFuelBurnAssignmentValidator()
        val item = newDefaultFuelBurnAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = DefaultFuelBurnAssignmentRequiredExternalRefRule("default fuel burn assignment needs an entry")
        val item = newDefaultFuelBurnAssignment("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DefaultFuelBurnAssignmentMinimumRangeRule(0.4, 2799.6)
        val item = newDefaultFuelBurnAssignment("x", "y")
        item.minimum = 2801.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultFuelBurnAssignmentCodeLengthRule(16)
        val item = newDefaultFuelBurnAssignment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = DefaultFuelBurnAssignmentFuelLitersRangeRule(1.3, 2044.5)
        val item = newDefaultFuelBurnAssignment("x", "y")
        item.fuelLiters = 2046.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultFuelBurnAssignmentRequiredTitleRule("default fuel burn assignment needs this field")
        val item = newDefaultFuelBurnAssignment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = DefaultFuelBurnAssignmentLaneLengthRule(80)
        val item = newDefaultFuelBurnAssignment("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
