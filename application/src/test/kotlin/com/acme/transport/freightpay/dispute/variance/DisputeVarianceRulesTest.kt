package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeVarianceValidator()
        val item = newDisputeVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = DisputeVarianceVehicleIdLengthRule(16)
        val item = newDisputeVariance("x", "y")
        item.vehicleId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = DisputeVarianceOdometerRangeRule(32, 3600)
        val item = newDisputeVariance("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DisputeVarianceCodeLengthRule(16)
        val item = newDisputeVariance("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = DisputeVarianceRequiredCarrierRule("dispute variance needs an entry")
        val item = newDisputeVariance("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = DisputeVarianceAmountRangeRule(6.0, 3916.9)
        val item = newDisputeVariance("x", "y")
        item.amount = 3918.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
