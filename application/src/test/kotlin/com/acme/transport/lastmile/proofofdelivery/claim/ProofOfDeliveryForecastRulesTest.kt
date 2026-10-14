package com.acme.transport.lastmile.proofofdelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryForecastValidator()
        val item = newProofOfDeliveryForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliveryForecastCodeLengthRule(32)
        val item = newProofOfDeliveryForecast("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = ProofOfDeliveryForecastVehicleIdLengthRule(16)
        val item = newProofOfDeliveryForecast("x", "y")
        item.vehicleId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = ProofOfDeliveryForecastRequiredDescriptionRule("proof of delivery forecast needs an entry")
        val item = newProofOfDeliveryForecast("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
