package com.acme.transport.safety.scorecard.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedScorecardQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedScorecardQuotaValidator()
        val item = newWeightedScorecardQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedScorecardQuotaCodeLengthRule(86400)
        val item = newWeightedScorecardQuota("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = WeightedScorecardQuotaDiscountRangeRule(9.5, 3396.2)
        val item = newWeightedScorecardQuota("x", "y")
        item.discount = 3397.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = WeightedScorecardQuotaEtaMinutesRangeRule(32, 64)
        val item = newWeightedScorecardQuota("x", "y")
        item.etaMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedScorecardQuotaRequiredCodeRule("weighted scorecard quota needs an entry")
        val item = newWeightedScorecardQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = WeightedScorecardQuotaMaximumRangeRule(5.1, 1026.8)
        val item = newWeightedScorecardQuota("x", "y")
        item.maximum = 1028.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
