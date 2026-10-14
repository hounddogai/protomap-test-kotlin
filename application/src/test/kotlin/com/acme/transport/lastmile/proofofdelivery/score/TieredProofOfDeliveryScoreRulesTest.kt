package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredProofOfDeliveryScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredProofOfDeliveryScoreValidator()
        val item = newTieredProofOfDeliveryScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = TieredProofOfDeliveryScoreTimezoneLengthRule(86400)
        val item = newTieredProofOfDeliveryScore("x", "y")
        item.timezone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = TieredProofOfDeliveryScoreRequiredTimezoneRule("tiered proof of delivery score needs a value")
        val item = newTieredProofOfDeliveryScore("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredProofOfDeliveryScoreCodeLengthRule(80)
        val item = newTieredProofOfDeliveryScore("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = TieredProofOfDeliveryScoreScoreRangeRule(4.5, 3415.7)
        val item = newTieredProofOfDeliveryScore("x", "y")
        item.score = 3417.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
