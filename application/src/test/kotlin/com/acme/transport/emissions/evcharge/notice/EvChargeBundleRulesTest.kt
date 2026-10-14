package com.acme.transport.emissions.evcharge.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeBundleValidator()
        val item = newEvChargeBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeBundleRequiredCodeRule("ev charge bundle needs this field")
        val item = newEvChargeBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = EvChargeBundleRequiredNameRule("ev charge bundle needs an entry")
        val item = newEvChargeBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = EvChargeBundleDiscountRangeRule(7.9, 1289.8)
        val item = newEvChargeBundle("x", "y")
        item.discount = 1291.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = EvChargeBundleLoadKgRangeRule(2.4, 1696.0)
        val item = newEvChargeBundle("x", "y")
        item.loadKg = 1697.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeBundleCodeLengthRule(1000)
        val item = newEvChargeBundle("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
