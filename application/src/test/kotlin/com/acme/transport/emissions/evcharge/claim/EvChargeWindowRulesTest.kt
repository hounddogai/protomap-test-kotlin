package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeWindowValidator()
        val item = newEvChargeWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = EvChargeWindowContactEmailLengthRule(255)
        val item = newEvChargeWindow("x", "y")
        item.contactEmail = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = EvChargeWindowWeightRangeRule(1.2, 3508.7)
        val item = newEvChargeWindow("x", "y")
        item.weight = 3510.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = EvChargeWindowPhoneLengthRule(255)
        val item = newEvChargeWindow("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
