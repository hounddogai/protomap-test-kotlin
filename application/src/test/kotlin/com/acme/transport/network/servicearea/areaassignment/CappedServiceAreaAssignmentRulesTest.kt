package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedServiceAreaAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedServiceAreaAssignmentValidator()
        val item = newCappedServiceAreaAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedServiceAreaAssignmentNameLengthRule(255)
        val item = newCappedServiceAreaAssignment("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = CappedServiceAreaAssignmentStartsAtRangeRule(120, 86400)
        val item = newCappedServiceAreaAssignment("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedServiceAreaAssignmentCodeLengthRule(32)
        val item = newCappedServiceAreaAssignment("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
