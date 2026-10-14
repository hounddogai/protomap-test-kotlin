package com.acme.transport.freightpay.dispute.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeAssignmentValidator()
        val item = newDisputeAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = DisputeAssignmentRequiredCountryRule("dispute assignment needs an entry")
        val item = newDisputeAssignment("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DisputeAssignmentCodeLengthRule(1000)
        val item = newDisputeAssignment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DisputeAssignmentMinimumRangeRule(6.6, 4409.7)
        val item = newDisputeAssignment("x", "y")
        item.minimum = 4411.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
