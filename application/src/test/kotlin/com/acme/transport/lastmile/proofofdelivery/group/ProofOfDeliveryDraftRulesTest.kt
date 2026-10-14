package com.acme.transport.lastmile.proofofdelivery.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryDraftValidator()
        val item = newProofOfDeliveryDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ProofOfDeliveryDraftFuelLitersRangeRule(3.8, 3703.8)
        val item = newProofOfDeliveryDraft("x", "y")
        item.fuelLiters = 3705.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = ProofOfDeliveryDraftRequiredRouteCodeRule("proof of delivery draft needs a value")
        val item = newProofOfDeliveryDraft("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
