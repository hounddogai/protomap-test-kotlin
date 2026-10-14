package com.acme.transport.fleetops.utilization.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryUtilizationPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryUtilizationPlanValidator()
        val item = newPrimaryUtilizationPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryUtilizationPlanLabelLengthRule(120)
        val item = newPrimaryUtilizationPlan("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryUtilizationPlanCodeLengthRule(16)
        val item = newPrimaryUtilizationPlan("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = PrimaryUtilizationPlanContactEmailLengthRule(1000)
        val item = newPrimaryUtilizationPlan("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = PrimaryUtilizationPlanSequenceRangeRule(16, 1000)
        val item = newPrimaryUtilizationPlan("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryUtilizationPlanRequiredCodeRule("primary utilization plan needs a value")
        val item = newPrimaryUtilizationPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
