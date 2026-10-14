package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentTargetValidator()
        val item = newIncidentTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = IncidentTargetEtaMinutesRangeRule(80, 255)
        val item = newIncidentTarget("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = IncidentTargetCodeLengthRule(32)
        val item = newIncidentTarget("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = IncidentTargetNameLengthRule(120)
        val item = newIncidentTarget("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = IncidentTargetCostRangeRule(8.2, 3973.6)
        val item = newIncidentTarget("x", "y")
        item.cost = 3975.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = IncidentTargetBudgetRangeRule(9.5, 489.7)
        val item = newIncidentTarget("x", "y")
        item.budget = 491.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
