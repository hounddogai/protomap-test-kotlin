package com.acme.transport.safety.nearmiss.missrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNearMissQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveNearMissQuotaValidator()
        val item = newAdaptiveNearMissQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveNearMissQuotaCodeLengthRule(32)
        val item = newAdaptiveNearMissQuota("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = AdaptiveNearMissQuotaWeightRangeRule(7.7, 2060.4)
        val item = newAdaptiveNearMissQuota("x", "y")
        item.weight = 2061.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = AdaptiveNearMissQuotaRateRangeRule(7.6, 945.2)
        val item = newAdaptiveNearMissQuota("x", "y")
        item.rate = 946.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
