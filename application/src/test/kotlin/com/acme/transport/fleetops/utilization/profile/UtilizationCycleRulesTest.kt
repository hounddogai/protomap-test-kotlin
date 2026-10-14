package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationCycleValidator()
        val item = newUtilizationCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = UtilizationCycleLabelLengthRule(120)
        val item = newUtilizationCycle("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = UtilizationCycleRequiredExternalRefRule("utilization cycle needs this field")
        val item = newUtilizationCycle("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = UtilizationCyclePriorityRangeRule(16, 3600)
        val item = newUtilizationCycle("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = UtilizationCycleExternalRefLengthRule(255)
        val item = newUtilizationCycle("x", "y")
        item.externalRef = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
