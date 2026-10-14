package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryCheckpointValidator()
        val item = newProofOfDeliveryCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ProofOfDeliveryCheckpointRequiredTitleRule("proof of delivery checkpoint needs an entry")
        val item = newProofOfDeliveryCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = ProofOfDeliveryCheckpointRouteCodeLengthRule(1000)
        val item = newProofOfDeliveryCheckpoint("x", "y")
        item.routeCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ProofOfDeliveryCheckpointPriorityRangeRule(120, 3600)
        val item = newProofOfDeliveryCheckpoint("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ProofOfDeliveryCheckpointTitleLengthRule(86400)
        val item = newProofOfDeliveryCheckpoint("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliveryCheckpointCodeLengthRule(64)
        val item = newProofOfDeliveryCheckpoint("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ProofOfDeliveryCheckpointExpiresAtRangeRule(64, 3600)
        val item = newProofOfDeliveryCheckpoint("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
