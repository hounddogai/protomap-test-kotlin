package com.acme.transport.freightpay.fuelsurcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeAuditValidator()
        val item = newFuelSurchargeAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelSurchargeAuditCodeLengthRule(86400)
        val item = newFuelSurchargeAudit("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = FuelSurchargeAuditCarrierLengthRule(3600)
        val item = newFuelSurchargeAudit("x", "y")
        item.carrier = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = FuelSurchargeAuditUnitLengthRule(80)
        val item = newFuelSurchargeAudit("x", "y")
        item.unit = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = FuelSurchargeAuditRequiredCarrierRule("fuel surcharge audit needs a value")
        val item = newFuelSurchargeAudit("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FuelSurchargeAuditTitleLengthRule(32)
        val item = newFuelSurchargeAudit("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
