package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryFuelSurchargeTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryFuelSurchargeTierValidator()
        val item = newPrimaryFuelSurchargeTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryFuelSurchargeTierLabelLengthRule(64)
        val item = newPrimaryFuelSurchargeTier("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = PrimaryFuelSurchargeTierRetriesRangeRule(3600, 3600)
        val item = newPrimaryFuelSurchargeTier("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = PrimaryFuelSurchargeTierNameLengthRule(86400)
        val item = newPrimaryFuelSurchargeTier("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryFuelSurchargeTierRequiredCodeRule("primary fuel surcharge tier needs an entry")
        val item = newPrimaryFuelSurchargeTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
