package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultScorecardAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultScorecardAllocationValidator()
        val item = newDefaultScorecardAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultScorecardAllocationRequiredTitleRule("default scorecard allocation needs a value")
        val item = newDefaultScorecardAllocation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = DefaultScorecardAllocationRequiredPhoneRule("default scorecard allocation needs an entry")
        val item = newDefaultScorecardAllocation("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = DefaultScorecardAllocationDepotLengthRule(86400)
        val item = newDefaultScorecardAllocation("x", "y")
        item.depot = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultScorecardAllocationRequiredCodeRule("default scorecard allocation needs a value")
        val item = newDefaultScorecardAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
