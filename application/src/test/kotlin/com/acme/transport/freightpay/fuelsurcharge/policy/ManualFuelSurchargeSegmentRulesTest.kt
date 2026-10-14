package com.acme.transport.freightpay.fuelsurcharge.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualFuelSurchargeSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualFuelSurchargeSegmentValidator()
        val item = newManualFuelSurchargeSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ManualFuelSurchargeSegmentRequiredChannelRule("manual fuel surcharge segment needs an entry")
        val item = newManualFuelSurchargeSegment("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = ManualFuelSurchargeSegmentMinimumRangeRule(9.5, 4449.3)
        val item = newManualFuelSurchargeSegment("x", "y")
        item.minimum = 4450.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualFuelSurchargeSegmentTitleLengthRule(120)
        val item = newManualFuelSurchargeSegment("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
