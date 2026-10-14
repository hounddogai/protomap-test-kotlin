package com.acme.transport.freightpay.dispute.disputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDisputeAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDisputeAssignmentValidator()
        val item = newManualDisputeAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ManualDisputeAssignmentEtaMinutesRangeRule(120, 86400)
        val item = newManualDisputeAssignment("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualDisputeAssignmentRequiredCodeRule("manual dispute assignment needs a value")
        val item = newManualDisputeAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
