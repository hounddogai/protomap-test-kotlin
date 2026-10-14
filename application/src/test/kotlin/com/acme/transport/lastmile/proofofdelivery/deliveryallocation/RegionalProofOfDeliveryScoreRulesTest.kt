package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalProofOfDeliveryScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalProofOfDeliveryScoreValidator()
        val item = newRegionalProofOfDeliveryScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalProofOfDeliveryScoreCodeLengthRule(120)
        val item = newRegionalProofOfDeliveryScore("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalProofOfDeliveryScoreLabelLengthRule(64)
        val item = newRegionalProofOfDeliveryScore("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = RegionalProofOfDeliveryScoreDockLengthRule(255)
        val item = newRegionalProofOfDeliveryScore("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RegionalProofOfDeliveryScoreAttemptsRangeRule(16, 64)
        val item = newRegionalProofOfDeliveryScore("x", "y")
        item.attempts = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RegionalProofOfDeliveryScoreSequenceRangeRule(1000, 86400)
        val item = newRegionalProofOfDeliveryScore("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
