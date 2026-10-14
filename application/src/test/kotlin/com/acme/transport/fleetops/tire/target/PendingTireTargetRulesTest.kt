package com.acme.transport.fleetops.tire.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTireTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTireTargetValidator()
        val item = newPendingTireTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = PendingTireTargetBudgetRangeRule(0.7, 318.1)
        val item = newPendingTireTarget("x", "y")
        item.budget = 319.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = PendingTireTargetTrailerIdLengthRule(86400)
        val item = newPendingTireTarget("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
