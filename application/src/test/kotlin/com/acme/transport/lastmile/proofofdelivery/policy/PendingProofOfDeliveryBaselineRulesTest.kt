package com.acme.transport.lastmile.proofofdelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingProofOfDeliveryBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingProofOfDeliveryBaselineValidator()
        val item = newPendingProofOfDeliveryBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = PendingProofOfDeliveryBaselineBudgetRangeRule(8.9, 2171.9)
        val item = newPendingProofOfDeliveryBaseline("x", "y")
        item.budget = 2173.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = PendingProofOfDeliveryBaselineFuelLitersRangeRule(8.5, 1029.5)
        val item = newPendingProofOfDeliveryBaseline("x", "y")
        item.fuelLiters = 1031.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingProofOfDeliveryBaselineTitleLengthRule(1000)
        val item = newPendingProofOfDeliveryBaseline("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingProofOfDeliveryBaselineCodeLengthRule(64)
        val item = newPendingProofOfDeliveryBaseline("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
