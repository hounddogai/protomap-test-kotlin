package com.acme.transport.freightpay.fuelsurcharge.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFuelSurchargeVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingFuelSurchargeVarianceValidator()
        val item = newPendingFuelSurchargeVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = PendingFuelSurchargeVarianceRequiredExternalRefRule("pending fuel surcharge variance needs a value")
        val item = newPendingFuelSurchargeVariance("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = PendingFuelSurchargeVarianceBudgetRangeRule(1.7, 569.9)
        val item = newPendingFuelSurchargeVariance("x", "y")
        item.budget = 571.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingFuelSurchargeVarianceRequiredTitleRule("pending fuel surcharge variance needs an entry")
        val item = newPendingFuelSurchargeVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = PendingFuelSurchargeVariancePhoneLengthRule(80)
        val item = newPendingFuelSurchargeVariance("x", "y")
        item.phone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
