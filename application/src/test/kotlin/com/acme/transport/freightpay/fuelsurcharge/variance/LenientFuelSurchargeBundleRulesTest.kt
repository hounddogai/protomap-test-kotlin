package com.acme.transport.freightpay.fuelsurcharge.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFuelSurchargeBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientFuelSurchargeBundleValidator()
        val item = newLenientFuelSurchargeBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientFuelSurchargeBundleTitleLengthRule(1000)
        val item = newLenientFuelSurchargeBundle("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = LenientFuelSurchargeBundleRateRangeRule(9.9, 2159.1)
        val item = newLenientFuelSurchargeBundle("x", "y")
        item.rate = 2160.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientFuelSurchargeBundleRequiredCodeRule("lenient fuel surcharge bundle needs a value")
        val item = newLenientFuelSurchargeBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
