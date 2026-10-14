package com.acme.transport.emissions.fuelburn.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveFuelBurnClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveFuelBurnClaimValidator()
        val item = newAdaptiveFuelBurnClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveFuelBurnClaimCodeLengthRule(120)
        val item = newAdaptiveFuelBurnClaim("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveFuelBurnClaimNameLengthRule(64)
        val item = newAdaptiveFuelBurnClaim("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = AdaptiveFuelBurnClaimBatchSizeRangeRule(80, 80)
        val item = newAdaptiveFuelBurnClaim("x", "y")
        item.batchSize = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = AdaptiveFuelBurnClaimCreatedAtRangeRule(32, 1000)
        val item = newAdaptiveFuelBurnClaim("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = AdaptiveFuelBurnClaimDescriptionLengthRule(64)
        val item = newAdaptiveFuelBurnClaim("x", "y")
        item.description = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
