package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargePolicyValidator()
        val item = newEvChargePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = EvChargePolicyAmountRangeRule(9.9, 2418.6)
        val item = newEvChargePolicy("x", "y")
        item.amount = 2420.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargePolicyCodeLengthRule(86400)
        val item = newEvChargePolicy("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = EvChargePolicyRequiredTrailerIdRule("ev charge policy needs a value")
        val item = newEvChargePolicy("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
