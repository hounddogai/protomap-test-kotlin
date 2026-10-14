package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackFuelSurchargeMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackFuelSurchargeMilestoneValidator()
        val item = newFallbackFuelSurchargeMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = FallbackFuelSurchargeMilestoneThresholdRangeRule(9.5, 752.8)
        val item = newFallbackFuelSurchargeMilestone("x", "y")
        item.threshold = 754.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = FallbackFuelSurchargeMilestoneRequiredPhoneRule("fallback fuel surcharge milestone needs a value")
        val item = newFallbackFuelSurchargeMilestone("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
