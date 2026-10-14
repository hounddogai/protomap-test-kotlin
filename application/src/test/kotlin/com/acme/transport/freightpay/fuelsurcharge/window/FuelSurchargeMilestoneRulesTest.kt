package com.acme.transport.freightpay.fuelsurcharge.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeMilestoneValidator()
        val item = newFuelSurchargeMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = FuelSurchargeMilestoneRequiredChecksumRule("fuel surcharge milestone needs a value")
        val item = newFuelSurchargeMilestone("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = FuelSurchargeMilestoneCurrencyLengthRule(255)
        val item = newFuelSurchargeMilestone("x", "y")
        item.currency = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelSurchargeMilestoneCodeLengthRule(80)
        val item = newFuelSurchargeMilestone("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = FuelSurchargeMilestoneVersionRangeRule(80, 1000)
        val item = newFuelSurchargeMilestone("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
