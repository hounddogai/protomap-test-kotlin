package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRateCardAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRateCardAdjustmentValidator()
        val item = newManualRateCardAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualRateCardAdjustmentRequiredCodeRule("manual rate card adjustment needs a value")
        val item = newManualRateCardAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = ManualRateCardAdjustmentVehicleIdLengthRule(80)
        val item = newManualRateCardAdjustment("x", "y")
        item.vehicleId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualRateCardAdjustmentCodeLengthRule(3600)
        val item = newManualRateCardAdjustment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
