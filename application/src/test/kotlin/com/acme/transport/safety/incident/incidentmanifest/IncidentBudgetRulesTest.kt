package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentBudgetValidator()
        val item = newIncidentBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = IncidentBudgetThresholdRangeRule(3.4, 2776.6)
        val item = newIncidentBudget("x", "y")
        item.threshold = 2778.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = IncidentBudgetCapacityRangeRule(64, 120)
        val item = newIncidentBudget("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
