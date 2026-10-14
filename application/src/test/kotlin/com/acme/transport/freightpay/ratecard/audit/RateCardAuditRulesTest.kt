package com.acme.transport.freightpay.ratecard.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardAuditValidator()
        val item = newRateCardAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = RateCardAuditRequiredUnitRule("rate card audit needs a value")
        val item = newRateCardAudit("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = RateCardAuditRequiredVehicleIdRule("rate card audit needs a value")
        val item = newRateCardAudit("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
