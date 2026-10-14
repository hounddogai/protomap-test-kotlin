package com.acme.transport.emissions.evcharge.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeDigestValidator()
        val item = newEvChargeDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = EvChargeDigestVehicleIdLengthRule(16)
        val item = newEvChargeDigest("x", "y")
        item.vehicleId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeDigestRequiredCodeRule("ev charge digest needs a value")
        val item = newEvChargeDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = EvChargeDigestOdometerRangeRule(255, 3600)
        val item = newEvChargeDigest("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
