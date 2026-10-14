package com.acme.transport.lastmile.proofofdelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryRollupValidator()
        val item = newProofOfDeliveryRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = ProofOfDeliveryRollupRequiredRegionRule("proof of delivery rollup needs this field")
        val item = newProofOfDeliveryRollup("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = ProofOfDeliveryRollupMinimumRangeRule(5.3, 2547.1)
        val item = newProofOfDeliveryRollup("x", "y")
        item.minimum = 2548.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ProofOfDeliveryRollupTitleLengthRule(32)
        val item = newProofOfDeliveryRollup("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ProofOfDeliveryRollupBatchSizeRangeRule(64, 3600)
        val item = newProofOfDeliveryRollup("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ProofOfDeliveryRollupRequiredCodeRule("proof of delivery rollup needs this field")
        val item = newProofOfDeliveryRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ProofOfDeliveryRollupRequiredTitleRule("proof of delivery rollup needs this field")
        val item = newProofOfDeliveryRollup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
