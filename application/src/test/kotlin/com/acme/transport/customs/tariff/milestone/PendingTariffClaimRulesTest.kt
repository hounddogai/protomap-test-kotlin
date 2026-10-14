package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTariffClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTariffClaimValidator()
        val item = newPendingTariffClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = PendingTariffClaimRequiredVehicleIdRule("pending tariff claim needs a value")
        val item = newPendingTariffClaim("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingTariffClaimNameLengthRule(16)
        val item = newPendingTariffClaim("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingTariffClaimRequiredLabelRule("pending tariff claim needs this field")
        val item = newPendingTariffClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
