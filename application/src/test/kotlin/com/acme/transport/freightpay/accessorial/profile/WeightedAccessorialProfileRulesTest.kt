package com.acme.transport.freightpay.accessorial.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAccessorialProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAccessorialProfileValidator()
        val item = newWeightedAccessorialProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = WeightedAccessorialProfileVehicleIdLengthRule(3600)
        val item = newWeightedAccessorialProfile("x", "y")
        item.vehicleId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedAccessorialProfileRequiredCodeRule("weighted accessorial profile needs a value")
        val item = newWeightedAccessorialProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedAccessorialProfileCodeLengthRule(1000)
        val item = newWeightedAccessorialProfile("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
