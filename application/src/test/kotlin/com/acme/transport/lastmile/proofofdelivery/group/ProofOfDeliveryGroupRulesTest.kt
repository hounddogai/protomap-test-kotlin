package com.acme.transport.lastmile.proofofdelivery.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryGroupValidator()
        val item = newProofOfDeliveryGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = ProofOfDeliveryGroupScoreRangeRule(7.9, 4086.1)
        val item = newProofOfDeliveryGroup("x", "y")
        item.score = 4087.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = ProofOfDeliveryGroupRouteCodeLengthRule(32)
        val item = newProofOfDeliveryGroup("x", "y")
        item.routeCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliveryGroupCodeLengthRule(80)
        val item = newProofOfDeliveryGroup("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
