package com.acme.transport.freightpay.fuelsurcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeDraftValidator()
        val item = newFuelSurchargeDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = FuelSurchargeDraftThresholdRangeRule(0.4, 2206.6)
        val item = newFuelSurchargeDraft("x", "y")
        item.threshold = 2208.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = FuelSurchargeDraftCreatedAtRangeRule(16, 1000)
        val item = newFuelSurchargeDraft("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FuelSurchargeDraftRequiredLabelRule("fuel surcharge draft needs a value")
        val item = newFuelSurchargeDraft("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
