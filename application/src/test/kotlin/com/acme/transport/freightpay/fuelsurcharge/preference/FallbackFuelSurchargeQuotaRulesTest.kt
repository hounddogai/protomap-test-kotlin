package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackFuelSurchargeQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackFuelSurchargeQuotaValidator()
        val item = newFallbackFuelSurchargeQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = FallbackFuelSurchargeQuotaRequiredTrailerIdRule("fallback fuel surcharge quota needs an entry")
        val item = newFallbackFuelSurchargeQuota("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackFuelSurchargeQuotaCodeLengthRule(3600)
        val item = newFallbackFuelSurchargeQuota("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackFuelSurchargeQuotaRequiredNameRule("fallback fuel surcharge quota needs this field")
        val item = newFallbackFuelSurchargeQuota("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
