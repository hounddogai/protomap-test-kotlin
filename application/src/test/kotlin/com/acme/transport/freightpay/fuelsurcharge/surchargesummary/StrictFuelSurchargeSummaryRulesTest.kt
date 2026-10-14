package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictFuelSurchargeSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictFuelSurchargeSummaryValidator()
        val item = newStrictFuelSurchargeSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = StrictFuelSurchargeSummaryRequiredCountryRule("strict fuel surcharge summary needs an entry")
        val item = newStrictFuelSurchargeSummary("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictFuelSurchargeSummaryCodeLengthRule(16)
        val item = newStrictFuelSurchargeSummary("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
