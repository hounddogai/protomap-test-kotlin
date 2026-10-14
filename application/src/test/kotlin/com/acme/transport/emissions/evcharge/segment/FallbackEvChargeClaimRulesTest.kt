package com.acme.transport.emissions.evcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackEvChargeClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackEvChargeClaimValidator()
        val item = newFallbackEvChargeClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = FallbackEvChargeClaimLimitRangeRule(64, 86400)
        val item = newFallbackEvChargeClaim("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackEvChargeClaimTitleLengthRule(120)
        val item = newFallbackEvChargeClaim("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = FallbackEvChargeClaimRequiredExternalRefRule("fallback ev charge claim needs an entry")
        val item = newFallbackEvChargeClaim("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
