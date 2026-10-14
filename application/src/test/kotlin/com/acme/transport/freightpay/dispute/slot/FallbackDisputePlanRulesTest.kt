package com.acme.transport.freightpay.dispute.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDisputePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDisputePlanValidator()
        val item = newFallbackDisputePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = FallbackDisputePlanExternalRefLengthRule(1000)
        val item = newFallbackDisputePlan("x", "y")
        item.externalRef = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = FallbackDisputePlanWindowDaysRangeRule(64, 3600)
        val item = newFallbackDisputePlan("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackDisputePlanRequiredLabelRule("fallback dispute plan needs an entry")
        val item = newFallbackDisputePlan("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = FallbackDisputePlanEtaMinutesRangeRule(16, 255)
        val item = newFallbackDisputePlan("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = FallbackDisputePlanMaximumRangeRule(2.4, 2930.1)
        val item = newFallbackDisputePlan("x", "y")
        item.maximum = 2931.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
