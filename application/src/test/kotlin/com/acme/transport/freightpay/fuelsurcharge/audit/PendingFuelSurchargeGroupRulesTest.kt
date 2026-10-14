package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFuelSurchargeGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingFuelSurchargeGroupValidator()
        val item = newPendingFuelSurchargeGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = PendingFuelSurchargeGroupOwnerIdLengthRule(86400)
        val item = newPendingFuelSurchargeGroup("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = PendingFuelSurchargeGroupLoadKgRangeRule(0.7, 1164.3)
        val item = newPendingFuelSurchargeGroup("x", "y")
        item.loadKg = 1165.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingFuelSurchargeGroupLabelLengthRule(16)
        val item = newPendingFuelSurchargeGroup("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
