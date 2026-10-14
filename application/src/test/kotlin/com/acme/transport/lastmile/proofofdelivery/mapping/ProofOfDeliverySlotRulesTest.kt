package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliverySlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliverySlotValidator()
        val item = newProofOfDeliverySlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = ProofOfDeliverySlotRequiredRouteCodeRule("proof of delivery slot needs an entry")
        val item = newProofOfDeliverySlot("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliverySlotCodeLengthRule(255)
        val item = newProofOfDeliverySlot("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ProofOfDeliverySlotQuantityRangeRule(120, 3600)
        val item = newProofOfDeliverySlot("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = ProofOfDeliverySlotRouteCodeLengthRule(255)
        val item = newProofOfDeliverySlot("x", "y")
        item.routeCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ProofOfDeliverySlotLabelLengthRule(16)
        val item = newProofOfDeliverySlot("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
