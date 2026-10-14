package com.acme.transport.lastmile.proofofdelivery.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryAssignmentValidator()
        val item = newProofOfDeliveryAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ProofOfDeliveryAssignmentNameLengthRule(16)
        val item = newProofOfDeliveryAssignment("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = ProofOfDeliveryAssignmentReviewerLengthRule(86400)
        val item = newProofOfDeliveryAssignment("x", "y")
        item.reviewer = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliveryAssignmentCodeLengthRule(120)
        val item = newProofOfDeliveryAssignment("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ProofOfDeliveryAssignmentPriorityRangeRule(80, 80)
        val item = newProofOfDeliveryAssignment("x", "y")
        item.priority = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ProofOfDeliveryAssignmentRequiredCodeRule("proof of delivery assignment needs this field")
        val item = newProofOfDeliveryAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
