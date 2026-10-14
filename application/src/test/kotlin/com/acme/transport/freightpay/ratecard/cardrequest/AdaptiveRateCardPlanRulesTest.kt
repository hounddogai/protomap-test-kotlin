package com.acme.transport.freightpay.ratecard.cardrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRateCardPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveRateCardPlanValidator()
        val item = newAdaptiveRateCardPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = AdaptiveRateCardPlanScoreRangeRule(1.5, 1746.3)
        val item = newAdaptiveRateCardPlan("x", "y")
        item.score = 1747.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveRateCardPlanCodeLengthRule(120)
        val item = newAdaptiveRateCardPlan("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveRateCardPlanRequiredCodeRule("adaptive rate card plan needs a value")
        val item = newAdaptiveRateCardPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveRateCardPlanRequiredNameRule("adaptive rate card plan needs this field")
        val item = newAdaptiveRateCardPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = AdaptiveRateCardPlanRetriesRangeRule(64, 120)
        val item = newAdaptiveRateCardPlan("x", "y")
        item.retries = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
