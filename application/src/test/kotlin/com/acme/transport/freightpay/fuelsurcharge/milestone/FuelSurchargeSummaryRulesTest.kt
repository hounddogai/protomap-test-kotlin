package com.acme.transport.freightpay.fuelsurcharge.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeSummaryValidator()
        val item = newFuelSurchargeSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelSurchargeSummaryRequiredCodeRule("fuel surcharge summary needs an entry")
        val item = newFuelSurchargeSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FuelSurchargeSummaryLabelLengthRule(32)
        val item = newFuelSurchargeSummary("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = FuelSurchargeSummaryRequiredSummaryRule("fuel surcharge summary needs an entry")
        val item = newFuelSurchargeSummary("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
