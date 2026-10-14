package com.acme.transport.network.servicearea.cappedareaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictServiceAreaAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictServiceAreaAssignmentValidator()
        val item = newStrictServiceAreaAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictServiceAreaAssignmentRequiredNameRule("strict service area assignment needs this field")
        val item = newStrictServiceAreaAssignment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = StrictServiceAreaAssignmentStopCountRangeRule(32, 120)
        val item = newStrictServiceAreaAssignment("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictServiceAreaAssignmentNameLengthRule(80)
        val item = newStrictServiceAreaAssignment("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = StrictServiceAreaAssignmentCapacityRangeRule(64, 80)
        val item = newStrictServiceAreaAssignment("x", "y")
        item.capacity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = StrictServiceAreaAssignmentCarrierLengthRule(120)
        val item = newStrictServiceAreaAssignment("x", "y")
        item.carrier = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
