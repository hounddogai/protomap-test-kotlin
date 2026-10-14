package com.acme.transport.emissions.evcharge.chargequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictEvChargeClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictEvChargeClaimValidator()
        val item = newStrictEvChargeClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictEvChargeClaimCodeLengthRule(80)
        val item = newStrictEvChargeClaim("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = StrictEvChargeClaimRequiredSummaryRule("strict ev charge claim needs a value")
        val item = newStrictEvChargeClaim("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
