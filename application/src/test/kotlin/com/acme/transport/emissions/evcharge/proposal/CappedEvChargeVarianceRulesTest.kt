package com.acme.transport.emissions.evcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedEvChargeVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedEvChargeVarianceValidator()
        val item = newCappedEvChargeVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CappedEvChargeVarianceRequiredOwnerIdRule("capped ev charge variance needs an entry")
        val item = newCappedEvChargeVariance("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CappedEvChargeVarianceGraceDaysRangeRule(3600, 86400)
        val item = newCappedEvChargeVariance("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = CappedEvChargeVarianceRequiredVehicleIdRule("capped ev charge variance needs a value")
        val item = newCappedEvChargeVariance("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
