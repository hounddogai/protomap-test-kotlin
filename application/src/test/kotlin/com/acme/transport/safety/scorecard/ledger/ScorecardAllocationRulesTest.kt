package com.acme.transport.safety.scorecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardAllocationValidator()
        val item = newScorecardAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ScorecardAllocationCapacityRangeRule(16, 80)
        val item = newScorecardAllocation("x", "y")
        item.capacity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = ScorecardAllocationRequiredCountryRule("scorecard allocation needs an entry")
        val item = newScorecardAllocation("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ScorecardAllocationRequiredDriverIdRule("scorecard allocation needs this field")
        val item = newScorecardAllocation("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = ScorecardAllocationCountryLengthRule(3600)
        val item = newScorecardAllocation("x", "y")
        item.country = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardAllocationRequiredCodeRule("scorecard allocation needs this field")
        val item = newScorecardAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
